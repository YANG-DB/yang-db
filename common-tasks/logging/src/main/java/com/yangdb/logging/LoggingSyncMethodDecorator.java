package com.yangdb.logging;

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
import com.yangdb.commons.function.supplier.ThrowingSupplier;
import com.yangdb.logging.mdc.*;
import com.yangdb.logging.mdc.Epoch;
import com.yangdb.logging.mdc.MDCWriter;
import com.yangdb.logging.mdc.MethodName;
import com.yangdb.logging.mdc.Sequence;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.NOPLogger;

import java.util.Collections;
import java.util.function.Function;

import static com.yangdb.logging.LogMessage.Level.error;
import static com.yangdb.logging.mdc.LogType.*;

public class LoggingSyncMethodDecorator<TResult> implements MethodDecorator<TResult, Boolean> {
    //region Constructors
    public LoggingSyncMethodDecorator(
            Logger logger,
            Marker marker,
            MethodName.Value methodName,
            MDCWriter primerMdcWriter,
            Function<TResult, MDCWriter> successMdcWriterFunction,
            Function<Throwable, MDCWriter> failureMdcWriterFunction,
            Iterable<LogMessage.Level> preInvocationLevels,
            Function<TResult, Iterable<LogMessage.Level>> successLevelsFunction,
            Function<Throwable, Iterable<LogMessage.Level>> failureLevelsFunction,
            Function<Throwable, Throwable> throwableFunction,
            String startMessage,
            String successMessage,
            String failureMessage) {
        this.logger = logger;
        this.marker = marker;
        this.methodName = methodName;

        this.primerMdcWriter = primerMdcWriter;
        this.successMdcWriterFunction = successMdcWriterFunction;
        this.failureMdcWriterFunction = failureMdcWriterFunction;

        this.preInvocationLevels = preInvocationLevels;
        this.successLevelsFunction = successLevelsFunction;
        this.failureLevelsFunction = failureLevelsFunction;

        this.throwableFunction = throwableFunction;

        this.startMessage = startMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }

    private LoggingSyncMethodDecorator() {}
    //endregion

    //region MethodDecorator Implementation
    @Override
    public TResult decorate(ThrowingSupplier<TResult> methodInvocationSupplier, ResultHandler<TResult, Boolean> resultHandler) throws Exception {
        this.primerMdcWriter.write();

        try {
            for(LogMessage.Level level : this.preInvocationLevels) {
                new LogMessage.MDC(this.logger, level, this.startMessage,
                        sequence, Epoch.FromNanos.now(), start, this.methodName)
                        .withMarker(this.marker)
                        .log();
            }

            TResult methodInvocationResult = methodInvocationSupplier.get();

            MDCWriter successMDcWriter = this.successMdcWriterFunction.apply(methodInvocationResult);
            if (successMDcWriter != null) {
                successMDcWriter.write();
            }

            for(LogMessage.Level level : this.successLevelsFunction.apply(methodInvocationResult)) {
                new LogMessage.MDC(this.logger, level, this.successMessage,
                        sequence, Epoch.FromNanos.now(), success, this.methodName)
                        .withMarker(this.marker)
                        .log();
            }

            return resultHandler.onSuccess(methodInvocationResult, true);
        } catch (Exception ex) {
            MDCWriter failureMdcWriter = this.failureMdcWriterFunction.apply(ex);
            if (failureMdcWriter != null) {
                failureMdcWriter.write();
            }

            for(LogMessage.Level level : this.failureLevelsFunction.apply(ex)) {
                new LogMessage.MDC(this.logger, level, this.failureMessage,
                        sequence, Epoch.FromNanos.now(), failure, this.methodName)
                        .withMarker(this.marker)
                        .with(this.throwableFunction.apply(ex)).log();
            }

            return resultHandler.onFailure(ex, false);
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private Marker marker;
    private MethodName.Value methodName;

    private MDCWriter primerMdcWriter;
    private Function<TResult, MDCWriter> successMdcWriterFunction;
    private Function<Throwable, MDCWriter> failureMdcWriterFunction;

    private Iterable<LogMessage.Level> preInvocationLevels;
    private Function<TResult, Iterable<LogMessage.Level>> successLevelsFunction;
    private Function<Throwable, Iterable<LogMessage.Level>> failureLevelsFunction;

    private Function<Throwable, Throwable> throwableFunction;

    private String startMessage;
    private String successMessage;
    private String failureMessage;

    private static MDCWriter sequence = Sequence.incr();
    //endregion

    public static class Builder<TResult> {
        //region Constructors
        public Builder() {
            this(NOPLogger.NOP_LOGGER, MethodName.Value.empty);
        }

        public Builder(Logger logger) {
            this(logger, MethodName.Value.empty);
        }

        public Builder(Logger logger, MethodName.Value methodName) {
            this.decorator = new LoggingSyncMethodDecorator<>();
            this.decorator.logger = logger;
            this.decorator.methodName = methodName;
            this.decorator.primerMdcWriter = MDCWriter.Noop.instance;
            this.decorator.successMdcWriterFunction = result -> MDCWriter.Noop.instance;
            this.decorator.failureMdcWriterFunction = ex -> MDCWriter.Noop.instance;
            this.decorator.preInvocationLevels = Collections.emptyList();
            this.decorator.successLevelsFunction = result -> Collections.emptyList();
            this.decorator.failureLevelsFunction = ex -> Collections.singletonList(error);
            this.decorator.throwableFunction = ex -> ex;
        }

        public Builder(Builder<TResult> other) {
            this.decorator = new LoggingSyncMethodDecorator<>();
            this.decorator.logger = other.decorator.logger;
            this.decorator.methodName = other.decorator.methodName;
            this.decorator.primerMdcWriter = other.decorator.primerMdcWriter;
            this.decorator.successMdcWriterFunction = other.decorator.successMdcWriterFunction;
            this.decorator.failureMdcWriterFunction = other.decorator.failureMdcWriterFunction;
            this.decorator.preInvocationLevels = other.decorator.preInvocationLevels;
            this.decorator.successLevelsFunction = other.decorator.successLevelsFunction;
            this.decorator.failureLevelsFunction = other.decorator.failureLevelsFunction;
            this.decorator.throwableFunction = other.decorator.throwableFunction;
            this.decorator.startMessage = other.decorator.startMessage;
            this.decorator.successMessage = other.decorator.successMessage;
            this.decorator.failureMessage = other.decorator.failureMessage;
        }
        //endregion

        //region Builder
        public Builder<TResult> logger(Logger logger) {
            this.decorator.logger = logger;
            return this;
        }

        public Builder<TResult> marker(Marker marker) {
            this.decorator.marker = marker;
            return this;
        }

        public Builder<TResult> methodName(MethodName.Value methodName) {
            this.decorator.methodName = methodName;
            return this;
        }

        public Builder<TResult> primerMdcWriter(MDCWriter primerMdcWriter) {
            this.decorator.primerMdcWriter = primerMdcWriter;
            return this;
        }

        public Builder<TResult> successMdcWriterFunction(Function<TResult, MDCWriter> successMdcWriterFunction) {
            this.decorator.successMdcWriterFunction = successMdcWriterFunction;
            return this;
        }

        public Builder<TResult> failureMdcWriterFunction(Function<Throwable, MDCWriter> failureMdcWriterFunction) {
            this.decorator.failureMdcWriterFunction = failureMdcWriterFunction;
            return this;
        }

        public Builder<TResult> startMessage(String startMessage) {
            this.decorator.startMessage = startMessage;
            return this;
        }

        public Builder<TResult> successMessage(String successMessage) {
            this.decorator.successMessage = successMessage;
            return this;
        }

        public Builder<TResult> failureMessage(String failureMessage) {
            this.decorator.failureMessage = failureMessage;
            return this;
        }

        public Builder<TResult> preInvocationLevel(LogMessage.Level preInvocationLevel) {
            this.decorator.preInvocationLevels = Collections.singletonList(preInvocationLevel);
            return this;
        }

        public Builder<TResult> preInvocationLevels(Iterable<LogMessage.Level> preInvocationLevels) {
            this.decorator.preInvocationLevels = preInvocationLevels;
            return this;
        }

        public Builder<TResult> successLevel(LogMessage.Level successLevel) {
            this.decorator.successLevelsFunction = result -> Collections.singletonList(successLevel);
            return this;
        }

        public Builder<TResult> successLevels(Iterable<LogMessage.Level> successLevels) {
            this.decorator.successLevelsFunction = result -> successLevels;
            return this;
        }

        public Builder<TResult> failureLevel(LogMessage.Level failureLevel) {
            this.decorator.successLevelsFunction = ex -> Collections.singletonList(failureLevel);
            return this;
        }

        public Builder<TResult> failureLevels(Iterable<LogMessage.Level> failureLevels) {
            this.decorator.failureLevelsFunction = ex -> failureLevels;
            return this;
        }

        public Builder<TResult> successLevelsFunction(Function<TResult, Iterable<LogMessage.Level>> successLevelsFunction) {
            this.decorator.successLevelsFunction = successLevelsFunction;
            return this;
        }

        public Builder<TResult> failureLevelsFunction(Function<Throwable, Iterable<LogMessage.Level>> failureLevelsFunction) {
            this.decorator.failureLevelsFunction = failureLevelsFunction;
            return this;
        }

        public Builder<TResult> throwableFunction(Function<Throwable, Throwable> throwableFunction) {
            this.decorator.throwableFunction = throwableFunction;
            return this;
        }

        public LoggingSyncMethodDecorator<TResult> build() {
            if (this.decorator.logger == null) {
                this.decorator.logger = NOPLogger.NOP_LOGGER;
            }

            if (this.decorator.startMessage == null) {
                this.decorator.startMessage = "start " + this.decorator.methodName.toString();
            }

            if (this.decorator.successMessage == null) {
                this.decorator.successMessage = "finish " + this.decorator.methodName.toString();
            }

            if (this.decorator.failureMessage == null) {
                this.decorator.failureMessage = "failed " + this.decorator.methodName.toString();
            }

            return this.decorator;
        }
        //endregion

        //region Fields
        private LoggingSyncMethodDecorator<TResult> decorator;
        //endregion
    }
}
