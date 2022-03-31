package com.yangdb.commons.task.domain;

/*-
 * #%L
 * commons
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

import com.yangdb.commons.task.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.yangdb.commons.util.GenericUtils.infere;

public class ExecutorServiceExecution<TResult, TContext> implements DomainTask.Execution<TResult, TContext> {
    //region Constructors
    public ExecutorServiceExecution(DomainTask.Execution<TResult, TContext> execution, ExecutorService executorService) {
        this.execution = execution;
        this.executorService = executorService;
    }
    //endregion

    //region Execution Implementation
    @Override
    public Task<TResult, TContext> getTask() {
        return this.execution.getTask();
    }

    @Override
    public void progress(int progress) {
        this.execution.progress(progress);
    }

    @Override
    public void then(DomainTask.Executor<TResult, TContext> executor) {
        this.executorService.submit(() -> {
            try {
                this.execution.then(executor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void then(DomainTask.StageExecutor executor) {
        this.executorService.submit(() -> {
            try {
                this.execution.then(executor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void thenComplete(DomainTask.CompletionExecutor<TResult> executor) {
        this.executorService.submit(() -> {
            try {
                this.execution.thenComplete(executor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletionStage<Void> thenAsync(DomainTask.StageExecutor executor) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AtomicBoolean executed = new AtomicBoolean(false);

        this.executorService.submit(() -> {
            try {
                this.execution.then(() -> {
                    try {
                        if (!executed.get()) {
                            executor.execute();
                        } else {
                            future.complete(null);
                        }
                    } finally {
                        executed.set(true);
                    }
                });

                this.completeFuture(future, this.getTask());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    @Override
    public CompletionStage<TResult> thenCompleteAsync(DomainTask.CompletionExecutor<TResult> executor) {
        CompletableFuture<TResult> future = new CompletableFuture<>();

        this.executorService.submit(() -> {
            try {
                this.execution.thenComplete(executor);
                this.completeFuture(future, this.getTask());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }
    //endregion

    //region Private Methods
    private <TResult1> void completeFuture(CompletableFuture<TResult1> future, Task<TResult, TContext> task) {
        switch (task.getState()) {
            case success:
                if (!future.isDone()) {
                    try {
                        future.complete(infere(task.getResult()));
                    } catch (Exception ex) {
                        future.completeExceptionally(ex);
                    }
                }
                break;

            case failed:
                if (!future.isDone()) {
                    future.completeExceptionally(this.getTask().getException());
                }
                break;

            case running:
            case canceling:
                if (!future.isDone()) {
                    future.complete(null);
                }
                break;

            case canceled:
                if (!future.isDone()) {
                    future.cancel(false);
                }
                break;
        }
    }
    //endregion

    //region Fields
    private final DomainTask.Execution<TResult, TContext> execution;
    private final ExecutorService executorService;
    //endregion
}
