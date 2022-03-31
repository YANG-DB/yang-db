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

import com.yangdb.commons.builders.GenericBuilder;
import com.yangdb.commons.task.BaseTask;
import com.yangdb.commons.task.Task;
import com.yangdb.commons.throwables.ThrowableUtils;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.yangdb.commons.util.GenericUtils.infere;

public class DomainTask<TResult, TContext> extends BaseTask<TResult, TContext> {
    public interface CompletionExecutor<TResult> {
        TResult execute() throws Exception;

        interface Void {
            void execute() throws Exception;
        }
    }

    public interface StageExecutor {
        void execute() throws Exception;
    }

    public interface Executor<TResult, TContext> {
        class Result<TResult> {
            //region Static
            public static <TResult> Result<TResult> notDone() {
                return infere(NOT_DONE);
            }

            public static <TResult> Result<TResult> emptyDone() {
                return infere(EMPTY_DONE);
            }

            private final static Result<Void> NOT_DONE = new Result<>(false);
            private final static Result<Void> EMPTY_DONE = new Result<>(true);
            //endregion

            //region Constructors
            public Result(TResult result) {
                this.result = result;
                this.isComplete = true;
            }

            public Result(boolean isComplete) {
                this.isComplete = isComplete;
            }
            //endregion

            //region Result Implementation
            public TResult getResult() {
                return this.result;
            }

            public boolean isComplete() {
                return this.isComplete;
            }
            //endregion

            //region Fields
            private TResult result;
            private final boolean isComplete;
            //endregion
        }

        Result<TResult> execute(Execution<TResult, TContext> execution) throws Exception;

        interface Void<TResult, TContext> {
            void execute(Execution<TResult, TContext> execution) throws Exception;
        }

        class Noop<TResult, TContext> implements Executor<TResult, TContext> {
            public static <TResult1, TContext1> Noop<TResult1, TContext1> getInstance() {
                return infere(instance);
            }
            private final static Noop<?, ?> instance = new Noop<>();

            //region Executor Implementation
            @Override
            public Result<TResult> execute(Execution<TResult, TContext> execution) throws Exception {
                return null;
            }
            //endregion
        }
    }

    public interface Execution<TResult, TContext> {
        Task<TResult, TContext> getTask();
        void progress(int progress);

        void then(Executor<TResult, TContext> executor);
        void then(StageExecutor executor);
        void thenComplete(CompletionExecutor<TResult> executor);

        CompletionStage<Void> thenAsync(StageExecutor executor);
        CompletionStage<TResult> thenCompleteAsync(CompletionExecutor<TResult> executor);

        default void thenFail(Exception ex) {
            this.thenComplete(() -> { throw ex; });
        }
        default CompletionStage<TResult> thenFailAsync(Exception ex) {
            return this.thenCompleteAsync(() -> { throw ex; });
        }

        class Noop<TResult, TContext> implements Execution<TResult, TContext> {
            public static <TResult1, TContext1> Noop<TResult1, TContext1> getInstance() {
                return infere(instance);
            }
            private static final Noop<?, ?> instance = new Noop<>();

            //region Constructors
            public Noop() {
                this.nullStage = CompletableFuture.completedFuture(null);
            }
            //endregion

            //region Execution Implementation
            @Override
            public Task<TResult, TContext> getTask() {
                return Task.Noop.getInstance();
            }

            @Override
            public void progress(int progress) {

            }

            @Override
            public void then(Executor<TResult, TContext> executor) {

            }

            @Override
            public void then(StageExecutor executor) {

            }

            @Override
            public void thenComplete(CompletionExecutor<TResult> executor) {

            }

            @Override
            public CompletionStage<Void> thenAsync(StageExecutor executor) {
                return infere(this.nullStage);
            }

            @Override
            public CompletionStage<TResult> thenCompleteAsync(CompletionExecutor<TResult> executor) {
                return infere(this.nullStage);
            }
            //endregion

            //region Fields
            private final CompletionStage<?> nullStage;
            //endregion
        }
    }

    //region Constructors
    private DomainTask(
            int id,
            String type,
            Clock clock,
            TContext context,
            Executor<TResult, TContext> executor,
            Execution<TResult, TContext> execution,
            Execution<TResult, TContext> startExecution,
            Execution<TResult, TContext> resumeExecution,
            Execution<TResult, TContext> completionExecution,
            List<Task> childTasks) {
        super(id, type, clock, context, childTasks);
        this.executor = executor;
        this.execution = execution;
        this.startExecution = startExecution;
        this.resumeExecution = resumeExecution;
        this.completionExecution = completionExecution;
    }
    //endregion

    //region BaseTask Implementation
    @Override
    protected void execute(Task.State previousState) throws Exception {
        switch (previousState) {
            case pending:
                Executor<TResult, TContext> startExecutor = this.executor;
                this.executor = execution -> {
                    this.startExecution.then(startExecutor);
                    return Executor.Result.notDone();
                };
                break;

            case paused:
                Executor<TResult, TContext> resumeExecutor = this.executor;
                this.executor = execution -> {
                    this.resumeExecution.then(resumeExecutor);
                    return Executor.Result.notDone();
                };
                break;
        }

        try {
            if (this.getState() != State.pausing && this.getState() != State.canceling) {
                Executor.Result<TResult> result = this.executor.execute(this.execution);
                if (result != null) {
                    if (result.getResult() != null || result.isComplete()) {
                        this.completionExecution.then(() -> this.complete(result.getResult()));
                    }
                }
            }
        } catch (Exception ex) {
            this.completionExecution.then(() -> this.complete(ex));
        }
    }
    //endregion

    //region Fields
    private Executor<TResult, TContext> executor;
    private Execution<TResult, TContext> execution;
    private Execution<TResult, TContext> startExecution;
    private Execution<TResult, TContext> resumeExecution;
    private Execution<TResult, TContext> completionExecution;
    //endregion

    private class ExecutionImpl implements Execution<TResult, TContext> {
        //region Execution Implementation
        @Override
        public Task<TResult, TContext> getTask() {
            return DomainTask.this;
        }

        @Override
        public void progress(int progress) {
            DomainTask.this.progress(progress);
        }

        @Override
        public void then(Executor<TResult, TContext> executor) {
            DomainTask.this.executor = executor;

            try {
                State state = this.getTask().getState();
                if (state == State.running || state == State.pausing || state == State.canceling) {
                    DomainTask.this.validateAndExecute(State.running, State.pausing, State.canceling);
                }
            } catch (Exception ex) {
                throw ThrowableUtils.toRuntimeException(ex);
            }
        }

        @Override
        public void then(StageExecutor executor) {
            this.then((execution) -> {
                executor.execute();
                return Executor.Result.notDone();
            });
        }

        @Override
        public void thenComplete(CompletionExecutor<TResult> executor) {
            this.then(execution -> new Executor.Result<>(executor.execute()));
        }

        @Override
        public CompletionStage<Void> thenAsync(StageExecutor executor) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            AtomicBoolean executed = new AtomicBoolean(false);

            try {
                this.then(execution -> {
                    try {
                        if (!executed.get()) {
                            executor.execute();
                        } else {
                            future.complete(null);
                        }

                        return Executor.Result.notDone();
                    } finally {
                        executed.set(true);
                    }
                });

                this.completeFuture(future, this.getTask());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }

            return future;
        }

        @Override
        public CompletionStage<TResult> thenCompleteAsync(CompletionExecutor<TResult> executor) {
            CompletableFuture<TResult> future = new CompletableFuture<>();

            try {
                this.then(execution -> new Executor.Result<>(executor.execute()));
                this.completeFuture(future, this.getTask());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }

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
    }

    public static class Builder<TResult, TContext> implements GenericBuilder<DomainTask<TResult, TContext>> {
        //region Constructors
        public Builder(Executor<TResult, TContext> executor) {
            this();
            this.executor(executor);
        }

        public Builder() {
            this.task = new DomainTask<>(0, null, Clock.systemUTC(), null, null, null, null, null, null, Collections.emptyList());
        }
        //endregion

        //region Builder Implementation
        public Builder<TResult, TContext> id(int id) {
            this.task.id = id;
            return this;
        }

        public Builder<TResult, TContext> type(String type) {
            this.task.type = type;
            return this;
        }

        public Builder<TResult, TContext> clock(Clock clock) {
            this.task.clock = clock;
            return this;
        }

        public Builder<TResult, TContext> context(TContext context) {
            this.task.context = context;
            return this;
        }

        public Builder<TResult, TContext> executor(Executor<TResult, TContext> executor) {
            this.task.executor = executor;
            return this;
        }

        public Builder<TResult, TContext> executor(Executor.Void<TResult, TContext> executor) {
            this.task.executor = execution -> {
                executor.execute(execution);
                return Executor.Result.notDone();
            };
            return this;
        }

        public Builder<TResult, TContext> completionExecutor(CompletionExecutor<TResult> executor) {
            this.task.executor = execution -> new Executor.Result<>(executor.execute());
            return this;
        }

        public Builder<TResult, TContext> completionExecutor(CompletionExecutor.Void executor) {
            this.task.executor = execution -> {
                executor.execute();
                return Executor.Result.emptyDone();
            };
            return this;
        }

        public Builder<TResult, TContext> result(TResult result) {
            this.task.executor = execution -> new Executor.Result<>(result);
            return this;
        }

        public Builder<TResult, TContext> execution(Function<Execution<TResult, TContext>, Execution<TResult, TContext>> executionFunction) {
            this.task.execution = executionFunction.apply(this.task.new ExecutionImpl());
            return this;
        }

        public Builder<TResult, TContext> startExecution(Function<Execution<TResult, TContext>, Execution<TResult, TContext>> executionFunction) {
            this.task.startExecution = executionFunction.apply(this.task.new ExecutionImpl());
            return this;
        }

        public Builder<TResult, TContext> resumeExecution(Function<Execution<TResult, TContext>, Execution<TResult, TContext>> executionFunction) {
            this.task.resumeExecution = executionFunction.apply(this.task.new ExecutionImpl());
            return this;
        }

        public Builder<TResult, TContext> completionExecution(Function<Execution<TResult, TContext>, Execution<TResult, TContext>> executionFunction) {
            this.task.completionExecution = executionFunction.apply(this.task.new ExecutionImpl());
            return this;
        }

        public Builder<TResult, TContext> childTasks(List<Task> childTasks) {
            this.task.childTasks = childTasks;
            return this;
        }

        @Override
        public <T2 extends DomainTask<TResult, TContext>> T2 build() {
            if (this.task.clock == null) {
                this.task.clock = Clock.systemUTC();
            }

            if (this.task.executor == null) {
                this.task.executor = execution -> Executor.Result.emptyDone();
            }

            if (this.task.execution == null) {
                this.task.execution = this.task.new ExecutionImpl();
            }

            if (this.task.startExecution == null) {
                this.task.startExecution = this.task.execution;
            }

            if (this.task.resumeExecution == null) {
                this.task.resumeExecution = this.task.execution;
            }

            if (this.task.completionExecution == null) {
                this.task.completionExecution = this.task.execution;
            }

            return infere(this.task);
        }
        //endregion

        //region Fields
        private final DomainTask<TResult, TContext> task;
        //endregion
    }
}
