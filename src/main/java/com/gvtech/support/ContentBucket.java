package com.gvtech.support;

import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ContentBucket<T> {

    private final ExecutorService executor;
    private final Semaphore semaphore;
    private final List<Future<T>> toRetrieve = new ArrayList<>();

    public void add(final GetContentTask<T> task) {
        this.toRetrieve.add(executor.submit(() -> {
            semaphore.acquire();
            try {
                return task.compute();
            } finally {
                semaphore.release();
            }
        }));
    }

    private T safeGet(Future<T> future) {
        try {
            return future.get(10000, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error("error getting");
            return null;
        }
    }

    public List<T> get() {
        final List<T> result = new ArrayList<>();
        for (Future<T> future : this.toRetrieve) {
            if (safeGet(future) == null) {
                continue;
            }
            result.add(safeGet(future));
        }

        return result;
    }
}
