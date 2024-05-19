package com.gvtech.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutorAware {

    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    protected void execute(final Runnable runnable) {
        this.executorService.submit(runnable);
    }

}
