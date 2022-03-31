package com.yangdb.logging.commons.concurrent;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.logging.slf4j.ExtendedMDC;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MDCExecutorService implements ExecutorService {
    //region Constructors
    public MDCExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
    //endregion

    //region ExecutorService Implementation
    @Override
    public void shutdown() {
        this.executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.executorService.submit(() -> {
            MDC.setContextMap(mdc);
            ExtendedMDC.setContextMap(extendedMDC);
            return task.call();
        });
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.executorService.submit(() -> {
            MDC.setContextMap(mdc);
            ExtendedMDC.setContextMap(extendedMDC);
            task.run();
            return result;
        });
    }

    @Override
    public Future<?> submit(Runnable task) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.executorService.submit(() -> {
            MDC.setContextMap(mdc);
            ExtendedMDC.setContextMap(extendedMDC);
            task.run();
        });
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();

        List<Callable<T>> mdcTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            mdcTasks.add(() -> {
                MDC.setContextMap(mdc);
                ExtendedMDC.setContextMap(extendedMDC);
                return task.call();
            });
        }

        return this.executorService.invokeAll(mdcTasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();

        List<Callable<T>> mdcTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            mdcTasks.add(() -> {
                MDC.setContextMap(mdc);
                ExtendedMDC.setContextMap(extendedMDC);
                return task.call();
            });
        }

        return this.executorService.invokeAll(mdcTasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();

        List<Callable<T>> mdcTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            mdcTasks.add(() -> {
                MDC.setContextMap(mdc);
                ExtendedMDC.setContextMap(extendedMDC);
                return task.call();
            });
        }

        return this.executorService.invokeAny(mdcTasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();

        List<Callable<T>> mdcTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            mdcTasks.add(() -> {
                MDC.setContextMap(mdc);
                ExtendedMDC.setContextMap(extendedMDC);
                return task.call();
            });
        }

        return this.executorService.invokeAny(mdcTasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();

        this.executorService.execute(() -> {
            MDC.setContextMap(mdc);
            ExtendedMDC.setContextMap(extendedMDC);
            command.run();
        });
    }
    //endregion

    //region Fields
    private ExecutorService executorService;
    //endregion
}
