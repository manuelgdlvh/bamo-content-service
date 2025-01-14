package com.gvtech.core.status;

import io.quarkus.logging.Log;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ContentStatus {

    private static final int INVALIDATE_TIME = 1000 * 60 * 15;
    private static final int AWAIT_TIME = 2000;
    private static final int MAX_RETRIES = 5;

    private final String id;
    private final Lock lock = new ReentrantLock();
    private final Condition changeSignal = lock.newCondition();


    // STATE
    private Status status = Status.IDLE;
    private int pendingReads = 0;
    private int pendingWrites = 0;
    private int pendingUpdates = 0;
    private long lastOperation = Instant.now().toEpochMilli();

    public ContentStatus(final String id) {
        this.id = id;
    }


    public boolean tryChangeStatus(final Status status) {
        return switch (status) {
            case READING -> read();
            case UPDATING -> update();
            case WRITING -> write();
            case null, default -> throw new IllegalArgumentException("INVALID STATUS CHANGE");
        };
    }


    private boolean write() {
        lock.lock();
        try {
            Log.info(String.format("trying to acquire write status lock for %s", id));

            if (isInvalidated()) {
                Log.warn(String.format("invalidated status for #%s write, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                return false;
            }

            int retries = 0;
            pendingWrites++;
            while (this.status != Status.IDLE) {
                if (retries == MAX_RETRIES) {
                    Log.warn(String.format("max retries reached waiting for #%s write, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                    pendingWrites--;
                    this.changeSignal.signalAll();
                    return false;
                }

                if (!safeAwait()) {
                    retries++;
                }
            }

            lastOperation = Instant.now().toEpochMilli();
            this.status = Status.WRITING;
        } finally {
            lock.unlock();
        }

        return true;
    }

    private boolean read() {
        lock.lock();
        try {
            Log.info(String.format("trying to acquire read status lock for %s", id));

            if (isInvalidated()) {
                Log.warn(String.format("invalidated status for #%s read, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                return false;
            }

            pendingReads++;
            int retries = 0;
            while (status != Status.IDLE && status != Status.READING) {
                if (retries == MAX_RETRIES) {
                    Log.warn(String.format("max retries reached waiting for #%s read, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                    pendingReads--;
                    this.changeSignal.signalAll();
                    return false;
                }

                if (!safeAwait()) {
                    retries++;
                }
            }

            lastOperation = Instant.now().toEpochMilli();
            this.status = Status.READING;
        } finally {
            lock.unlock();
        }

        return true;
    }


    private boolean update() {
        lock.lock();
        try {
            Log.info(String.format("trying to acquire update status lock for %s", id));

            if (isInvalidated()) {
                Log.warn(String.format("invalidated status for #%s update, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                return false;
            }

            pendingUpdates++;
            int retries = 0;
            while (this.status != Status.IDLE || this.pendingWrites > 0) {
                if (retries == MAX_RETRIES) {
                    Log.warn(String.format("max retries reached waiting for #%s update, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                    pendingUpdates--;
                    this.changeSignal.signalAll();
                    return false;
                }

                if (!safeAwait()) {
                    retries++;
                }
            }

            lastOperation = Instant.now().toEpochMilli();
            this.status = Status.UPDATING;
        } finally {
            lock.unlock();
        }

        return true;
    }

    private boolean safeAwait() {
        try {
            return this.changeSignal.await(AWAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.error(String.format("an error ocurred waiting to status change for #%s caused by: %s", id, e.getMessage()));
            return false;
        }
    }


    public void endRead() {
        lock.lock();
        try {
            if (--pendingReads == 0) {
                status = Status.IDLE;
            }
            this.changeSignal.signalAll();
            Log.info(String.format("end read for #%s content, signaling all waiting threads", id));
        } finally {
            lock.unlock();
        }
    }

    public void endUpdate() {
        lock.lock();
        try {
            pendingUpdates--;
            status = Status.IDLE;
            this.changeSignal.signalAll();
            Log.info(String.format("end update for #%s content, signaling all waiting threads", id));
        } finally {
            lock.unlock();
        }
    }


    public void endWrite() {
        lock.lock();
        try {
            pendingWrites--;
            status = Status.IDLE;
            this.changeSignal.signalAll();
            Log.info(String.format("end write for #%s content, signaling all waiting threads", id));
        } finally {
            lock.unlock();
        }
    }


    public int totalPendingOperations() {
        return this.pendingUpdates + this.pendingWrites + pendingReads;
    }

    public boolean tryInvalidate() {

        lock.lock();
        try {
            if (totalPendingOperations() > 0) {
                Log.info(String.format("cannot invalidate status lock for #%s, pending operations [read = %s, write = %s, update = %s]", id, pendingReads, pendingWrites, pendingUpdates));
                return false;
            }

            final long now = Instant.now().toEpochMilli();
            if (lastOperation + INVALIDATE_TIME > now) {
                Log.info(String.format("cannot invalidate status lock for #%s, no expiration time passed", id));
                return false;
            }

            this.status = Status.INVALIDATE;
        } finally {
            lock.unlock();
        }

        return true;
    }

    private boolean isInvalidated() {
        return this.status == Status.INVALIDATE;
    }

}
