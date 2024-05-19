package com.gvtech.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class AbstractContentListProvider {
    private static final int MAX_CONCURRENT_CALLS = 120;

    private static final ExecutorService worker = Executors.newVirtualThreadPerTaskExecutor();
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_CALLS);


    public <T> ContentBucket<T> emptyBucket() {
        return new ContentBucket<>(worker, semaphore);
    }


}
