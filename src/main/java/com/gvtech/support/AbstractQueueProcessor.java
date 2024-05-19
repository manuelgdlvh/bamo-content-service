package com.gvtech.support;

import io.quarkus.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public abstract class AbstractQueueProcessor<T> extends AsyncExecutorAware {
    private final ExecutorService worker;
    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private final long pollTimeout;
    private final int maxCount;


    public AbstractQueueProcessor(final int maxCount, final long pollTimeout) {
        this.pollTimeout = pollTimeout;
        this.maxCount = maxCount;
        this.worker = Executors.newSingleThreadExecutor();
        init();
    }


    private void init() {
        this.worker.submit(() -> {
            while (true) {
                safeRun();
            }
        });
    }

    private void run() throws InterruptedException {
        final T initialItem = this.queue.poll(15000, TimeUnit.MILLISECONDS);
        if (initialItem == null) {
            return;
        }

        final List<T> items = new ArrayList<>();
        items.add(initialItem);
        while (items.size() < maxCount) {
            final T item = this.queue.poll(pollTimeout, TimeUnit.MILLISECONDS);
            if (item == null) {
                break;
            }

            items.add(item);
        }

        process(items);
    }

    private void safeRun() {
        try {
            run();
        } catch (Exception e) {
        }
    }


    public void enqueue(final T item) {
        safeEnqueue(item);
    }

    private void safeEnqueue(final T item) {
        try {
            if (!this.queue.offer(item, 200, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("TIMEOUT ERROR");
            }
        } catch (Exception e) {
        }
    }


    protected abstract void process(final List<T> items) throws InterruptedException;

}
