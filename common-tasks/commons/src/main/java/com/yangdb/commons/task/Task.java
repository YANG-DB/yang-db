package com.yangdb.commons.task;

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

import com.yangdb.commons.function.consumer.ThrowingConsumer;
import com.yangdb.commons.util.GenericUtils;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface Task<TResult, TContext> {
    enum State {
        pending,
        running,
        pausing,
        paused,
        canceling,
        canceled,
        success,
        failed
    }

    int getId();
    <TTask extends Task<TResult, TContext>> TTask setId(int id);

    <TParent, TParentContext, PTask extends Task<TParent, TParentContext>> PTask getParentTask();
    <TParent, TParentContext, PTask extends Task<TParent, TParentContext>, TTask extends Task<TResult, TContext>> TTask setParentTask(PTask parentTask);

    <CResult, CContext, CTask extends Task<CResult, CContext>> Collection<CTask> getChildTasks();

    String getType();
    State getState();
    <TTask extends Task<TResult, TContext>> TTask onStateChange(ThrowingConsumer<Task<TResult, TContext>> consumer);

    int getProgress();

    long getTimestamp();
    long getElapsed();

    <T extends TResult> T getResult() throws Exception;
    Exception getException();

    <T extends TContext> T getContext();

    <TTask extends Task<TResult, TContext>> TTask start();
    <TTask extends Task<TResult, TContext>> TTask pause();
    <TTask extends Task<TResult, TContext>> TTask resume();
    <TTask extends Task<TResult, TContext>> TTask cancel();

    <TTask extends Task<TResult, TContext>> TTask await();
    <TTask extends Task<TResult, TContext>> TTask await(long timeout);

    <TTask extends Task<TResult, TContext>> TTask then(ThrowingConsumer<Task<TResult, TContext>> consumer);

    default CompletionStage<TResult> getFuture() {
        CompletableFuture<TResult> future = new CompletableFuture<>();
        switch (this.getState()) {
            case success:
                try {
                    future.complete(this.getResult());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
                break;

            case failed:
                future.completeExceptionally(this.getException());
                break;

            case canceled:
                future.cancel(true);
                break;

            default:
                this.then(task1 -> {
                    switch (task1.getState()) {
                        case success:
                            try {
                                future.complete(task1.getResult());
                            } catch (Exception ex) {
                                future.completeExceptionally(ex);
                            }
                            break;

                        case failed:
                            future.completeExceptionally(task1.getException());
                            break;

                        case canceled:
                            future.cancel(true);
                            break;

                        default:
                            throw new IllegalStateException("Task was completed but did'nt enter a completion state.");
                    }
                });
        }

        return future;
    }

    class Noop<TResult, TContext> implements Task<TResult, TContext> {
        public static <TResult, TContext> Noop<TResult, TContext> getInstance() {
            return GenericUtils.infere(instance);
        }
        private final static Noop<Void, Void> instance = new Noop<>();

        //region Task Implementation
        @Override
        public int getId() {
            return 0;
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask setId(int id) {
            return null;
        }

        @Override
        public <TParent, TParentContext, PTask extends Task<TParent, TParentContext>> PTask getParentTask() {
            return null;
        }

        @Override
        public <TParent, TParentContext, PTask extends Task<TParent, TParentContext>, TTask extends Task<TResult, TContext>> TTask setParentTask(PTask parentTask) {
            return null;
        }

        @Override
        public <CResult, CContext, CTask extends Task<CResult, CContext>> Collection<CTask> getChildTasks() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public State getState() {
            return null;
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask onStateChange(ThrowingConsumer<Task<TResult, TContext>> consumer) {
            return GenericUtils.infere(this);
        }

        @Override
        public int getProgress() {
            return 0;
        }

        @Override
        public long getTimestamp() {
            return 0;
        }

        @Override
        public long getElapsed() {
            return 0;
        }

        @Override
        public <T extends TResult> T getResult() throws Exception {
            return null;
        }

        @Override
        public Exception getException() {
            return null;
        }

        @Override
        public <T extends TContext> T getContext() {
            return null;
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask start() {
            return GenericUtils.infere(this);
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask pause() {
            return GenericUtils.infere(this);
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask resume() {
            return GenericUtils.infere(this);
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask cancel() {
            return GenericUtils.infere(this);
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask await() {
            return GenericUtils.infere(this);
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask await(long timeout) {
            return GenericUtils.infere(this);
        }

        @Override
        public <TTask extends Task<TResult, TContext>> TTask then(ThrowingConsumer<Task<TResult, TContext>> consumer) {
            return GenericUtils.infere(this);
        }
        //endregion
    }
}
