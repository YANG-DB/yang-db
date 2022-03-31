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

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class SynchronizedTask<TResult, TContext> implements Task<TResult, TContext> {
    //region Constructors
    public SynchronizedTask(Task<TResult, TContext> task, Lock lock, long lockAcquireTimeout, TimeUnit lockAcquireTimeUnit) {
        this.task = task;
        this.lock = lock;
        this.lockAcquireTimeout = lockAcquireTimeout;
        this.lockAcquireTimeUnit = lockAcquireTimeUnit;
        this.task.then(task1 -> this.lock.unlock());
    }
    //endregion

    //region Task Implementation
    @Override
    public int getId() {
        return this.task.getId();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask setId(int id) {
        return this.task.setId(id);
    }

    @Override
    public <TParent, TParentContext, PTask extends Task<TParent, TParentContext>> PTask getParentTask() {
        return this.task.getParentTask();
    }

    @Override
    public <TParent, TParentContext, PTask extends Task<TParent, TParentContext>, TTask extends Task<TResult, TContext>> TTask setParentTask(PTask parentTask) {
        return this.task.setParentTask(parentTask);
    }

    @Override
    public <CResult, CContext, CTask extends Task<CResult, CContext>> Collection<CTask> getChildTasks() {
        return this.task.getChildTasks();
    }

    @Override
    public String getType() {
        return this.task.getType();
    }

    @Override
    public State getState() {
        return this.task.getState();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask onStateChange(ThrowingConsumer<Task<TResult, TContext>> consumer) {
        return this.task.onStateChange(consumer);
    }

    @Override
    public int getProgress() {
        return this.task.getProgress();
    }

    @Override
    public long getTimestamp() {
        return this.task.getTimestamp();
    }

    @Override
    public long getElapsed() {
        return this.task.getElapsed();
    }

    @Override
    public <T extends TResult> T getResult() throws Exception {
        return this.task.getResult();
    }

    @Override
    public Exception getException() {
        return this.task.getException();
    }

    @Override
    public <T extends TContext> T getContext() {
        return this.task.getContext();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask start() {
        try {
            if (!this.lock.tryLock(this.lockAcquireTimeout, this.lockAcquireTimeUnit)) {
                throw new RuntimeException("failed to acquire lock for task execution after ");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        return this.task.start();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask pause() {
        return this.task.pause();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask resume() {
        return this.task.resume();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask cancel() {
        return this.task.cancel();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask await() {
        return this.task.await();
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask await(long timeout) {
        return this.task.await(timeout);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask then(ThrowingConsumer<Task<TResult, TContext>> consumer) {
        return this.task.then(consumer);
    }
    //endregion


    //region Fields
    private final Task<TResult, TContext> task;
    private final Lock lock;
    private final long lockAcquireTimeout;
    private final TimeUnit lockAcquireTimeUnit;
    //endregion
}
