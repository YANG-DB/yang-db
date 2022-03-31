package com.yangdb.logging.commons.task;

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

import com.yangdb.commons.function.MethodDecorator;
import com.yangdb.commons.function.consumer.ThrowingConsumer;
import com.yangdb.logging.LogMessage;
import com.yangdb.logging.LoggingSyncMethodDecorator;
import com.yangdb.logging.mdc.MDCWriter;
import com.yangdb.logging.mdc.MethodName;
import com.yangdb.commons.task.Task;
import com.yangdb.logging.slf4j.ExtendedMDC;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static com.yangdb.commons.util.GenericUtils.infere;

public class LoggingTask<TResult, TContext> implements Task<TResult, TContext> {
    //region Constructors
    public LoggingTask(
            Task<TResult, TContext> task,
            Logger logger,
            Marker marker,
            MethodName.Value methodName,
            MDCWriter primerMdcWriter,
            Function<TResult, MDCWriter> successMdcWriterFunction,
            Function<Throwable, MDCWriter> failureMdcWriterFunction,
            Iterable<LogMessage.Level> preInvocationLevels,
            Iterable<LogMessage.Level> onSuccessLevels) {
        this.task = task;
        this.logger = logger;
        this.marker = marker;
        this.methodName = methodName;
        this.primerMdcWriter = primerMdcWriter;
        this.successMdcWriterFunction = successMdcWriterFunction;
        this.failureMdcWriterFunction = failureMdcWriterFunction;
        this.preInvocationLevels = preInvocationLevels;
        this.onSuccessLevels = onSuccessLevels;

        this.task.onStateChange(this.getStateChangeConsumer());
        this.task.then(this.getCompletionConsumer());
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
    public  <CResult, CContext, CTask extends Task<CResult, CContext>> Collection<CTask> getChildTasks() {
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
        this.task.onStateChange(consumer);
        return infere(this);
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
            this.getStartConsumer().accept(this.task);
            return infere(this);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask pause() {
        this.task.pause();
        return infere(this);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask resume() {
        if (this.mdcCopy != null) {
            MDC.setContextMap(this.mdcCopy);
            this.mdcCopy = null;
        }

        if (this.extendedMdcCopy != null) {
            ExtendedMDC.setContextMap(this.extendedMdcCopy);
            this.extendedMdcCopy = null;
        }

        this.task.resume();
        return infere(this);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask cancel() {
        this.task.cancel();
        return infere(this);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask await() {
        this.task.await();
        return infere(this);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask await(long timeout) {
        this.task.await(timeout);
        return infere(this);
    }

    @Override
    public <TTask extends Task<TResult, TContext>> TTask then(ThrowingConsumer<Task<TResult, TContext>> consumer) {
        this.task.then(consumer);
        return infere(this);
    }
    //endregion

    //region Private Methods
    private ThrowingConsumer<Task<TResult, TContext>> getStartConsumer() {
        return task1 -> new LoggingSyncMethodDecorator.Builder<Task<TResult, TContext>>(this.logger, this.methodName)
                .marker(this.marker)
                .preInvocationLevels(this.preInvocationLevels)
                .primerMdcWriter(this.primerMdcWriter)
                .failureMdcWriterFunction(this.failureMdcWriterFunction)
                .startMessage("start task " + task1.getType())
                .successMessage("finish task " + task1.getType())
                .failureMessage("failed task " + task1.getType()).build()
                .decorate(task1::start, new MethodDecorator.ResultHandler.Passthrough<>(ex -> task1));
    }

    private ThrowingConsumer<Task<TResult, TContext>> getCompletionConsumer() {
        return task1 -> new LoggingSyncMethodDecorator.Builder<TResult>(this.logger, this.methodName)
                .marker(this.marker)
                .successLevels(this.onSuccessLevels)
                .successMdcWriterFunction(this.successMdcWriterFunction)
                .failureMdcWriterFunction(this.failureMdcWriterFunction)
                .startMessage("start task " + task1.getType())
                .successMessage("finish task " + task1.getType())
                .failureMessage("failed task " + task1.getType()).build()
                .decorate(task1::getResult, new MethodDecorator.ResultHandler.Passthrough<>(ex -> null));
    }

    private ThrowingConsumer<Task<TResult, TContext>> getStateChangeConsumer() {
        return task1 -> {
            if (task1.getState() == State.paused) {
                this.mdcCopy = MDC.getCopyOfContextMap();
                this.extendedMdcCopy = ExtendedMDC.getCopyOfContextMap();
            } else if (task1.getState() == State.running) {
                if (this.mdcCopy != null) {
                    MDC.setContextMap(this.mdcCopy);
                    this.mdcCopy = null;
                }

                if (this.extendedMdcCopy != null) {
                    ExtendedMDC.setContextMap(this.extendedMdcCopy);
                    this.extendedMdcCopy = null;
                }
            }
        };
    }
    //endregion

    //region Field
    private final Task<TResult, TContext> task;
    private final Logger logger;
    private Marker marker;

    private final MethodName.Value methodName;
    private MDCWriter primerMdcWriter;
    private Function<TResult, MDCWriter> successMdcWriterFunction;
    private Function<Throwable, MDCWriter> failureMdcWriterFunction;
    private Iterable<LogMessage.Level> preInvocationLevels;
    private Iterable<LogMessage.Level> onSuccessLevels;

    private Map<String, String> mdcCopy;
    private Map<String, Object> extendedMdcCopy;
    //endregion

    public static class Builder<TResult, TContext> {
        //region Constructors
        public Builder(Logger logger) {
            this.loggingTask = new LoggingTask<>(
                    Task.Noop.getInstance(),
                    logger,
                    null,
                    taskVirtualMethod,
                    MDCWriter.Noop.instance,
                    result -> MDCWriter.Noop.instance,
                    ex -> MDCWriter.Noop.instance,
                    Collections.singletonList(LogMessage.Level.trace),
                    Collections.singletonList(LogMessage.Level.trace));
        }
        //endregion

        //region Builder

        public LoggingTask.Builder<TResult, TContext> marker(Marker marker) {
            this.loggingTask.marker = marker;
            return this;
        }

        public LoggingTask.Builder<TResult, TContext> primerMdcWriter(MDCWriter primerMdcWriter) {
            this.loggingTask.primerMdcWriter = primerMdcWriter;
            return this;
        }

        public LoggingTask.Builder<TResult, TContext> successMdcWriterFunction(Function<TResult, MDCWriter> successMdcWriterFunction) {
            this.loggingTask.successMdcWriterFunction = successMdcWriterFunction;
            return this;
        }

        public LoggingTask.Builder<TResult, TContext> failureMdcWriterFunction(Function<Throwable, MDCWriter> failureMdcWriterFunction) {
            this.loggingTask.failureMdcWriterFunction = failureMdcWriterFunction;
            return this;
        }

        public LoggingTask.Builder<TResult, TContext> preInvocationLevels(Iterable<LogMessage.Level> preInvocationLevels) {
            this.loggingTask.preInvocationLevels = preInvocationLevels;
            return this;
        }

        public LoggingTask.Builder<TResult, TContext> onSuccessLevels(Iterable<LogMessage.Level> onSuccessLevels) {
            this.loggingTask.onSuccessLevels = onSuccessLevels;
            return this;
        }

        public LoggingTask<TResult, TContext> build(Task<TResult, TContext> task) {
            return new LoggingTask<>(task,
                    this.loggingTask.logger,
                    this.loggingTask.marker,
                    this.loggingTask.methodName,
                    this.loggingTask.primerMdcWriter,
                    this.loggingTask.successMdcWriterFunction,
                    this.loggingTask.failureMdcWriterFunction,
                    this.loggingTask.preInvocationLevels,
                    this.loggingTask.onSuccessLevels);
        }
        //endregion

        //region Fields
        private final LoggingTask<TResult, TContext> loggingTask;
        private final static MethodName.Value taskVirtualMethod = MethodName.of("task");
        //endregion
    }
}
