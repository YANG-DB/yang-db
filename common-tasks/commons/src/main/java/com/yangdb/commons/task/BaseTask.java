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

import com.yangdb.commons.function.atomic.AtomicFunction;
import com.yangdb.commons.function.atomic.AtomicRunnable;
import com.yangdb.commons.function.consumer.ThrowingConsumer;
import com.yangdb.commons.function.supplier.ThrowingSupplier;
import com.yangdb.commons.util.GenericUtils;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;


public abstract class BaseTask<TResult, TContext> implements Task<TResult, TContext> {
    //region Constructors
    public BaseTask(int id, String type, Clock clock) {
        this(id, type, clock, null, Collections.emptyList());
    }

    public BaseTask(int id, String type, Clock clock, TContext context) {
        this(id, type, clock, context, Collections.emptyList());
    }

    public BaseTask(int id, String type, Clock clock, TContext context, Task...childTasks) {
        this(id, type, clock, context, Arrays.asList(childTasks));
    }

    public BaseTask(int id, String type, Clock clock, TContext context, List<Task> childTasks) {
        this.id = id;
        this.type = type;
        this.stateConsumers = Collections.emptyList();
        this.completionConsumers = Collections.emptyList();
        this.state = State.pending;
        this.context = context;
        this.childTasks = childTasks;
        this.clock = clock;
        this.timestamp = this.clock.millis();

        this.atomicValidateAndUpdateState = new AtomicFunction<>(this::validateAndUpdateTaskState);
        this.atomicEnsureFuture = new AtomicRunnable(this::ensureFutureAtomic);
    }
    //endregion

    //region Task Implementation
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask setId(int id) {
        this.id = id;
        return (TTask)this;
    }

    @Override
    public <TParent, TParentContext, PTask extends Task<TParent, TParentContext>> PTask getParentTask() {
        return (PTask)this.parentTask;
    }

    @Override
    public <TParent, TParentContext, PTask extends Task<TParent, TParentContext>, TTask extends Task<TResult, TContext>> TTask setParentTask(PTask parentTask) {
        this.parentTask = parentTask;
        return (TTask)this;
    }

    @Override
    public <CResult, CContext, CTask extends Task<CResult, CContext>> Collection<CTask> getChildTasks() {
        if (this.childTasks == null || this.childTasks.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(GenericUtils.infere(this.childTasks));
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public long getElapsed() {
        switch (this.getState()) {
            case running:
            case pausing:
            case paused:
            case canceling:
                return this.clock.millis() - this.timestamp;
            default:
                return this.elapsed;
        }
    }

    @Override
    public <T extends TResult> T getResult() throws Exception {
        if (this.exception != null) {
            throw this.exception;
        }

        return (T)this.result;
    }

    @Override
    public Exception getException() {
        return this.exception;
    }

    @Override
    public <T extends TContext> T getContext() {
        return (T)this.context;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask start() {
        return this.validateAndExecute(State.pending);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask pause() {
        this.ensureState(State.running);
        this.setState(State.pausing);
        return (TTask)this;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask resume() {
        return this.validateAndExecute(State.paused);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask cancel() {
        this.ensureStates(State.pending, State.running, State.pausing, State.paused);
        if (this.getState() == State.pending || this.getState() == State.paused) {
            this.setState(State.canceling);
            this.complete((TResult)null);
        } else {
            this.setState(State.canceling);
        }
        return (TTask)this;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask await() {
        if (this.isComplete()) {
            this.ensureFutureComplete();
            return (TTask)this;
        }

        this.ensureFuture();

        if (this.isComplete()) {
            this.ensureFutureComplete();
            return (TTask)this;
        }

        try { this.future.get(); } catch (Exception ignored) {}

        return (TTask)this;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask await(long timeout) {
        if (timeout > 0) {
            if (this.isComplete()) {
                this.ensureFutureComplete();
                return (TTask) this;
            }

            this.ensureFuture();

            if (this.isComplete()) {
                this.ensureFutureComplete();
                return (TTask) this;
            }

            try {
                this.future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {
            }
        } else if (timeout < 0) {
            return this.await();
        }

        return (TTask)this;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask onStateChange(ThrowingConsumer<Task<TResult, TContext>> consumer) {
        if (this.stateConsumers.isEmpty()) {
            this.stateConsumers = new ArrayList<>();
        }

        this.stateConsumers.add(consumer);
        return (TTask)this;
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask then(ThrowingConsumer<Task<TResult, TContext>> consumer) {
        if (this.completionConsumers.isEmpty()) {
            this.completionConsumers = new ArrayList<>();
        }

        this.completionConsumers.add(consumer);
        return (TTask)this;
    }

    @Override
    public CompletionStage<TResult> getFuture() {
        if (this.future == null) {
            this.ensureFuture();
            if (this.isComplete()) {
                if (!this.future.isDone()) {
                    this.ensureFutureComplete();
                }
            }
        }
        return this.future;
    }
    //endregion

    //region Protected Methods
    void setState(State state) {
        this.state = state;

        try {
            this.invokeStateConsumers();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected boolean isComplete() {
        return this.getState() == State.success || this.getState() == State.failed || this.getState() == State.canceled;
    }

    protected <TTask extends Task<TResult, TContext>> TTask progress(int progress) {
        if (progress > 100) {
            progress = 100;
        } else if (progress < 0) {
            progress = 0;
        }

        this.ensureStates(State.running, State.pausing, State.canceling);
        this.progress = progress;
        return (TTask)this;
    }

    protected <TTask extends Task<TResult, TContext>> TTask complete(TResult result) {
        this.ensureStates(State.running, State.pausing, State.canceling);
        this.setState(this.getState() == State.canceling ? State.canceled : State.success);
        if (this.getState() != State.canceled) {
            this.progress = 100;
        }
        this.elapsed = this.clock.millis() - this.timestamp;
        this.result = this.getState() == State.success ? result : null;
        this.completeImpl();
        return (TTask)this;
    }

    protected <TTask extends Task<TResult, TContext>> TTask complete(Exception exception) {
        this.ensureStates(State.running, State.pausing, State.canceling);
        this.progress = 100;
        this.setState(State.failed);
        this.elapsed = this.clock.millis() - this.timestamp;
        this.exception = exception;
        this.completeImpl();
        return (TTask)this;
    }

    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<TResult, ?> task) {
        return this.completeWith(task, (ThrowingSupplier<TResult>) task::getResult, ex -> ex);
    }
    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<?, ?> task, TResult result) {
        return this.completeWith(task, ThrowingSupplier.wrap(() -> result), ex -> ex);
    }
    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<?, ?> task, TResult result, Function<Exception, Exception> exceptionFunction) {
        return this.completeWith(task, ThrowingSupplier.wrap(() -> result), exceptionFunction);
    }
    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<?, ?> task, Supplier<TResult> resultSupplier) {
        return this.completeWith(task, ThrowingSupplier.wrap(resultSupplier), ex -> ex);
    }
    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<?, ?> task, Supplier<TResult> resultSupplier, Function<Exception, Exception> exceptionFunction) {
        return this.completeWith(task, ThrowingSupplier.wrap(resultSupplier), exceptionFunction);
    }
    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<?, ?> task, ThrowingSupplier<TResult> resultSupplier) {
        return this.completeWith(task, resultSupplier, ex -> ex);
    }
    protected <TTask extends Task<TResult, TContext>> TTask completeWith(Task<?, ?> task, ThrowingSupplier<TResult> resultSupplier, Function<Exception, Exception> exceptionFunction) {
        task.then(task1 -> {
            if (task1.getState() == State.success) {
                try {
                    this.complete(resultSupplier.get());
                } catch (Exception ex) {
                    this.complete(ex);
                }
            } else if (task1.getState() == State.failed) {
                this.complete(exceptionFunction.apply(task1.getException()));
            } else if (task1.getState() == State.canceled) {
                this.complete((TResult)null);
            }
        });

        return (TTask)this;
    }

    protected abstract void execute(State previousState) throws Exception;

    protected <TTask extends Task<TResult, TContext>> TTask validateAndExecute(State...validStates) {
        State previousState = this.atomicValidateAndUpdateState.apply(validStates);
        if (this.getState() == State.running) {
            try {
                this.execute(previousState);
                if (this.getState() == State.pausing) {
                    this.setState(State.paused);
                } else if (this.getState() == State.canceling) {
                    this.complete((TResult) null);
                }
            } catch (Exception ex) {
                this.complete(ex);
            }
        }

        return (TTask)this;
    }

    protected void completeImpl() {
        this.ensureFutureComplete();

        try {
            this.invokeCompletionConsumers();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void ensureState(State state) {
        if (this.getState() != state) {
            throw new IllegalStateException("Task expected state: " + state + " but actual state: " + this.getState());
        }
    }

    protected void ensureStates(State...states) {
        for(State state : states) {
            if (this.getState().equals(state)) {
                return;
            }
        }

        StringBuilder expectedStatesString = new StringBuilder();
        for(State state : states) {
            expectedStatesString.append(state).append(", ");
        }
        expectedStatesString.delete(expectedStatesString.length() - 2, expectedStatesString.length());

        throw new IllegalStateException("Task expected states: " + expectedStatesString + " but actual state: " + this.getState());
    }

    protected void ensureFutureComplete() {
        if (this.future != null && !this.future.isDone()) {
            if (this.getState() == State.success) {
                this.future.complete(this.result);
            } else if (this.getState() == State.failed) {
                this.future.completeExceptionally(this.exception);
            } else if (this.getState() == State.canceled) {
                try {
                    this.future.cancel(false);
                } catch (CancellationException ignored) {}
            }
        }
    }

    protected void invokeCompletionConsumers() throws Exception {
        for(ThrowingConsumer<Task<TResult, TContext>> consumer : this.completionConsumers) {
            consumer.accept(this);
        }
    }

    protected void invokeStateConsumers() throws Exception {
        for(ThrowingConsumer<Task<TResult, TContext>> consumer : this.stateConsumers) {
            consumer.accept(this);
        }
    }

    @SuppressWarnings("unchecked")
    protected <TTask extends Task<TResult, TContext>> TTask addChildTasks(Task...childTasks) {
        for(Task childTask : childTasks) {
            if (!(this.childTasks instanceof ArrayList)) {
                this.childTasks = new ArrayList<>();
            }

            this.childTasks.add(childTask);
            childTask.setParentTask(this);
        }

        return (TTask)this;
    }

    protected <TTask extends Task<TResult, TContext>> TTask addChildTasks(List<Task> childTasks) {
        for(Task childTask : childTasks) {
            if (!(this.childTasks instanceof ArrayList)) {
                this.childTasks = new ArrayList<>();
            }

            this.childTasks.add(childTask);
            childTask.setParentTask(this);
        }

        return (TTask)this;
    }

    @SuppressWarnings("unchecked")
    protected <TTask extends Task<TResult, TContext>> TTask removeChildTasks(Task... childTasks) {
        for(Task childTask : childTasks) {
            Task currentParent = childTask.getParentTask();
            if (currentParent == this) {
                childTask.setParentTask(null);
            }

            this.childTasks.remove(childTask);
        }

        return (TTask)this;
    }

    protected <TTask extends Task<TResult, TContext>> TTask removeChildTasks(List<Task> childTasks) {
        for(Task childTask : childTasks) {
            Task currentParent = childTask.getParentTask();
            if (currentParent == this) {
                childTask.setParentTask(null);
            }

            this.childTasks.remove(childTask);
        }

        return (TTask)this;
    }
    //endregion

    //region Private Methods
    private State validateAndUpdateTaskState(State[] validStates) {
        this.ensureStates(validStates);

        State previousState = this.state;
        if (this.getState() == State.pausing) {
            this.setState(State.paused);
        } else if (this.getState() == State.canceling) {
            this.complete((TResult)null);
        } else {
            this.setState(State.running);
        }

        return previousState;
    }

    private void ensureFuture() {
        if (this.future == null) {
            this.atomicEnsureFuture.run();
        }
    }

    private void ensureFutureAtomic() {
        if (this.future == null) {
            this.future = new CompletableFuture<>();
        }
    }
    //endregion

    //region Fields
    protected int id;
    protected long timestamp;
    protected long elapsed;
    protected String type;
    State state;
    protected int progress;
    protected TResult result;
    protected Exception exception;
    protected TContext context;
    protected List<Task> childTasks;

    protected Task<?, ?> parentTask;

    protected CompletableFuture<TResult> future;

    protected List<ThrowingConsumer<Task<TResult, TContext>>> completionConsumers;
    protected List<ThrowingConsumer<Task<TResult, TContext>>> stateConsumers;

    protected Clock clock;

    private final Function<State[], State> atomicValidateAndUpdateState;
    private final Runnable atomicEnsureFuture;
    //endregion
}
