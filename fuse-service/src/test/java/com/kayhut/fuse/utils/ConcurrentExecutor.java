package com.kayhut.fuse.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Creates a executor with Fluent Interface :)
 */
class ConcurrentExecutor {

    private int requests = 1;
    private int threads = 2; // Default number of threads
    private long maxTimeWaiting = Long.MAX_VALUE;

    static ConcurrentExecutor newInstance() {
        return new ConcurrentExecutor();
    }

    ConcurrentExecutor requests(int requests) {
        this.requests = requests;
        return this;
    }

    ConcurrentExecutor threads(int threads) {
        this.threads = threads;
        return this;
    }

    ConcurrentExecutor timeoutMillis(long maxTimeWaiting) {
        this.maxTimeWaiting = maxTimeWaiting;
        return this;
    }

    void execute(Callable<Void> callable) throws AssertionError {
        ExecutorService threadPool = Executors.newScheduledThreadPool(threads);
        List<Callable<Void>> callableList = new ArrayList<>();
        for (int i = 0; i < requests; i++) {
            callableList.add(callable);
        }
        try {
            threadPool.invokeAll(callableList);
            threadPool.awaitTermination(maxTimeWaiting, MILLISECONDS);
        } catch (InterruptedException e) {
            throw new AssertionError(" Timeout exceeded! -> " + maxTimeWaiting);
        } finally {
            threadPool.shutdown();
        }
    }
}