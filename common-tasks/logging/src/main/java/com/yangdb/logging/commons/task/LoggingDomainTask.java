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

import com.yangdb.commons.builders.GenericBuilder;
import com.yangdb.commons.task.Task;
import com.yangdb.commons.task.domain.DomainTask;
import com.yangdb.logging.LogMessage;
import com.yangdb.logging.LoggingSyncMethodDecorator;
import com.yangdb.logging.mdc.MDCWriter;
import com.yangdb.logging.mdc.MethodName;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.NOPLogger;

import java.util.Collections;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static com.yangdb.commons.util.GenericUtils.infere;

public class LoggingDomainTask {
    public static class Execution<TResult, TContext> implements DomainTask.Execution<TResult, TContext> {
        //region Constructors
        public Execution(
                DomainTask.Execution<TResult, TContext> execution,
                Logger logger,
                Marker marker,
                MDCWriter primerMdcWriter,
                Function<TResult, MDCWriter> successMdcWriterFunction,
                Function<Throwable, MDCWriter> failureMdcWriterFunction,
                Iterable<LogMessage.Level> preInvocationLevels,
                Iterable<LogMessage.Level> onSuccessLevels) {
            this.execution = execution;
            this.logger = logger;
            this.marker = marker;
            this.primerMdcWriter = primerMdcWriter;
            this.successMdcWriterFunction = successMdcWriterFunction;
            this.failureMdcWriterFunction = failureMdcWriterFunction;
            this.preInvocationLevels = preInvocationLevels;
            this.onSuccessLevels = onSuccessLevels;
        }
        //endregion

        //region DomainTask.Execution Implementation
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
            this.execution.then(execution -> new LoggingSyncMethodDecorator.Builder<DomainTask.Executor.Result<TResult>>()
                    .logger(this.logger)
                    .marker(this.marker)
                    .methodName(execute)
                    .primerMdcWriter(this.primerMdcWriter)
                    .failureMdcWriterFunction(this.failureMdcWriterFunction)
                    .preInvocationLevels(this.preInvocationLevels)
                    .successLevels(onSuccessLevels)
                    .build()
                    .decorate(() -> executor.execute(execution)));
        }

        @Override
        public void then(DomainTask.StageExecutor executor) {
            this.execution.then(() -> new LoggingSyncMethodDecorator.Builder<Void>()
                    .logger(this.logger)
                    .marker(this.marker)
                    .methodName(execute)
                    .primerMdcWriter(this.primerMdcWriter)
                    .failureMdcWriterFunction(this.failureMdcWriterFunction)
                    .preInvocationLevels(this.preInvocationLevels)
                    .successLevels(onSuccessLevels)
                    .build()
                    .decorate(() -> {
                        executor.execute();
                        return null;
                    }));
        }

        @Override
        public void thenComplete(DomainTask.CompletionExecutor<TResult> executor) {
            this.execution.thenComplete(() -> new LoggingSyncMethodDecorator.Builder<TResult>()
                    .logger(this.logger)
                    .marker(this.marker)
                    .methodName(execute)
                    .primerMdcWriter(this.primerMdcWriter)
                    .successMdcWriterFunction(this.successMdcWriterFunction)
                    .failureMdcWriterFunction(this.failureMdcWriterFunction)
                    .preInvocationLevels(this.preInvocationLevels)
                    .successLevels(onSuccessLevels)
                    .build()
                    .decorate(executor::execute));
        }

        @Override
        public CompletionStage<Void> thenAsync(DomainTask.StageExecutor executor) {
            return this.execution.thenAsync(() -> new LoggingSyncMethodDecorator.Builder<Void>()
                    .logger(this.logger)
                    .marker(this.marker)
                    .methodName(execute)
                    .primerMdcWriter(this.primerMdcWriter)
                    .failureMdcWriterFunction(this.failureMdcWriterFunction)
                    .preInvocationLevels(this.preInvocationLevels)
                    .successLevels(onSuccessLevels)
                    .build()
                    .decorate(() -> {
                        executor.execute();
                        return null;
                    }));
        }

        @Override
        public CompletionStage<TResult> thenCompleteAsync(DomainTask.CompletionExecutor<TResult> executor) {
            return this.execution.thenCompleteAsync(() -> new LoggingSyncMethodDecorator.Builder<TResult>()
                    .logger(this.logger)
                    .marker(this.marker)
                    .methodName(execute)
                    .primerMdcWriter(this.primerMdcWriter)
                    .successMdcWriterFunction(this.successMdcWriterFunction)
                    .failureMdcWriterFunction(this.failureMdcWriterFunction)
                    .preInvocationLevels(this.preInvocationLevels)
                    .successLevels(onSuccessLevels)
                    .build()
                    .decorate(executor::execute));
        }
        //endregion

        //region Fields
        private DomainTask.Execution<TResult, TContext> execution;
        private Logger logger;
        private Marker marker;

        private MDCWriter primerMdcWriter;
        private Function<TResult, MDCWriter> successMdcWriterFunction;
        private Function<Throwable, MDCWriter> failureMdcWriterFunction;
        private Iterable<LogMessage.Level> preInvocationLevels;
        private Iterable<LogMessage.Level> onSuccessLevels;

        private static final MethodName.Value execute = MethodName.of("execute");
        //endregion

        public static class Builder<TResult, TContext> implements GenericBuilder<Execution<TResult, TContext>> {
            //region Constructors
            public Builder() {
                this.loggingExecution = new Execution<>(
                        DomainTask.Execution.Noop.getInstance(),
                        NOPLogger.NOP_LOGGER,
                        null,
                        MDCWriter.Noop.instance,
                        result -> MDCWriter.Noop.instance,
                        ex -> MDCWriter.Noop.instance,
                        Collections.singletonList(LogMessage.Level.trace),
                        Collections.singletonList(LogMessage.Level.trace));
            }
            //endregion

            //region Builder
            public Builder<TResult, TContext> execution(DomainTask.Execution<TResult, TContext> execution) {
                this.loggingExecution.execution = execution;
                return this;
            }

            public Builder<TResult, TContext> logger(Logger logger) {
                this.loggingExecution.logger = logger;
                return this;
            }

            public Builder<TResult, TContext> marker(Marker marker) {
                this.loggingExecution.marker = marker;
                return this;
            }

            public Builder<TResult, TContext> primerMdcWriter(MDCWriter primerMdcWriter) {
                this.loggingExecution.primerMdcWriter = primerMdcWriter;
                return this;
            }

            public Builder<TResult, TContext> successMdcWriterFunction(Function<TResult, MDCWriter> successMdcWriterFunction) {
                this.loggingExecution.successMdcWriterFunction = successMdcWriterFunction;
                return this;
            }

            public Builder<TResult, TContext> failureMdcWriterFunction(Function<Throwable, MDCWriter> failureMdcWriterFunction) {
                this.loggingExecution.failureMdcWriterFunction = failureMdcWriterFunction;
                return this;
            }

            public Builder<TResult, TContext> preInvocationLevels(Iterable<LogMessage.Level> preInvocationLevels) {
                this.loggingExecution.preInvocationLevels = preInvocationLevels;
                return this;
            }

            public Builder<TResult, TContext> onSuccessLevels(Iterable<LogMessage.Level> onSuccessLevels) {
                this.loggingExecution.onSuccessLevels = onSuccessLevels;
                return this;
            }

            @Override
            public <T2 extends Execution<TResult, TContext>> T2 build() {
                return infere(this.loggingExecution);
            }
            //endregion

            //region Fields
            private final Execution<TResult, TContext> loggingExecution;
            //endregion
        }
    }
}
