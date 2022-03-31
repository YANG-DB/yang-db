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

import com.yangdb.commons.builders.GenericBuilder;
import com.yangdb.commons.throwables.ThrowableUtils;
import com.yangdb.logging.LogMessage;
import com.yangdb.logging.LoggingSyncMethodDecorator;
import com.yangdb.logging.mdc.MDCWriter;
import com.yangdb.logging.mdc.MethodName;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.yangdb.commons.util.GenericUtils.infere;

public class LoggingExecutorService implements ExecutorService {
    //region Constructors
    public LoggingExecutorService(ExecutorService executorService, Logger logger) {
        this.executorService = executorService;
        this.logger = logger;

        this.submitMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.logger, submit).build();
        this.executeMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.logger, execute).build();
    }
    //endregion

    //region Executor Service Implementation
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
        return this.executorService.submit(() -> {
            try {
                return infere(this.submitMethodDecorator.decorate(task::call));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return this.executorService.submit(() -> {
            try {
                return infere(this.submitMethodDecorator.decorate(() -> {
                    task.run();
                    return result;
                }));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.executorService.submit(() -> {
            try {
                this.submitMethodDecorator.decorate(() -> {
                    task.run();
                    return null;
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Callable<T>> loggingTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            loggingTasks.add(() -> {
                try {
                    return infere(this.submitMethodDecorator.decorate(task::call));
                } catch (Exception ex) {
                    throw ThrowableUtils.toRuntimeException(ex);
                }
            });
        }

        return this.executorService.invokeAll(loggingTasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<Callable<T>> loggingTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            loggingTasks.add(() -> {
                try {
                    return infere(this.submitMethodDecorator.decorate(task::call));
                } catch (Exception ex) {
                    throw ThrowableUtils.toRuntimeException(ex);
                }
            });
        }

        return this.executorService.invokeAll(loggingTasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<Callable<T>> loggingTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            loggingTasks.add(() -> {
                try {
                    return infere(this.submitMethodDecorator.decorate(task::call));
                } catch (Exception ex) {
                    throw ThrowableUtils.toRuntimeException(ex);
                }
            });
        }

        return this.executorService.invokeAny(loggingTasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        List<Callable<T>> loggingTasks = new ArrayList<>();
        for(Callable<T> task : tasks) {
            loggingTasks.add(() -> {
                try {
                    return infere(this.submitMethodDecorator.decorate(task::call));
                } catch (Exception ex) {
                    throw ThrowableUtils.toRuntimeException(ex);
                }
            });
        }

        return this.executorService.invokeAny(loggingTasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        this.executorService.execute(() -> {
            try {
                this.executeMethodDecorator.decorate(() -> {
                    command.run();
                    return null;
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    //endregion

    //region Fields
    private ExecutorService executorService;
    protected Logger logger;
    protected LoggingSyncMethodDecorator<Object> submitMethodDecorator;
    protected LoggingSyncMethodDecorator<Object> executeMethodDecorator;

    protected static MethodName.Value submit = MethodName.of("submit");
    protected static MethodName.Value execute = MethodName.of("execute");
    //endregion

    public static class Builder implements GenericBuilder<ExecutorService> {
        //region Constructors
        public Builder(ExecutorService executorService, Logger logger) {
            this.loggingExecutorService = new LoggingExecutorService(executorService, logger);
            this.decoratorBuilder = new LoggingSyncMethodDecorator.Builder<>(logger, submit);
        }
        //endregion

        //region GenericBuilder Implementation
        public LoggingExecutorService.Builder primerMdcWriter(MDCWriter primerMdcWriter) {
            this.decoratorBuilder.primerMdcWriter(primerMdcWriter);
            return this;
        }

        public LoggingExecutorService.Builder successMdcWriterFunction(Function<Object, MDCWriter> successMdcWriterFunction) {
            this.decoratorBuilder.successMdcWriterFunction(successMdcWriterFunction);
            return this;
        }

        public LoggingExecutorService.Builder failureMdcWriterFunction(Function<Throwable, MDCWriter> failureMdcWriterFunction) {
            this.decoratorBuilder.failureMdcWriterFunction(failureMdcWriterFunction);
            return this;
        }

        public LoggingExecutorService.Builder startMessage(String startMessage) {
            this.decoratorBuilder.startMessage(startMessage);
            return this;
        }

        public LoggingExecutorService.Builder successMessage(String successMessage) {
            this.decoratorBuilder.successMessage(successMessage);
            return this;
        }

        public LoggingExecutorService.Builder failureMessage(String failureMessage) {
            this.decoratorBuilder.failureMessage(failureMessage);
            return this;
        }

        public LoggingExecutorService.Builder preInvocationLevels(Iterable<LogMessage.Level> preInvocationLevels) {
            this.decoratorBuilder.preInvocationLevels(preInvocationLevels);
            return this;
        }

        public LoggingExecutorService.Builder successLevels(Iterable<LogMessage.Level> successLevels) {
            this.decoratorBuilder.successLevels(successLevels);
            return this;
        }

        public LoggingExecutorService.Builder failureLevels(Iterable<LogMessage.Level> failureLevels) {
            this.decoratorBuilder.failureLevels(failureLevels);
            return this;
        }

        public LoggingExecutorService.Builder successLevelsFunction(Function<Object, Iterable<LogMessage.Level>> successLevelsFunction) {
            this.decoratorBuilder.successLevelsFunction(successLevelsFunction);
            return this;
        }

        public LoggingExecutorService.Builder failureLevelsFunction(Function<Throwable, Iterable<LogMessage.Level>> failureLevelsFunction) {
            this.decoratorBuilder.failureLevelsFunction(failureLevelsFunction);
            return this;
        }

        public LoggingExecutorService.Builder throwableFunction(Function<Throwable, Throwable> throwableFunction) {
            this.decoratorBuilder.throwableFunction(throwableFunction);
            return this;
        }

        @Override
        public <T2 extends ExecutorService> T2 build() {
            LoggingExecutorService loggingExecutorService = new LoggingExecutorService(
                    this.loggingExecutorService.executorService,
                    this.loggingExecutorService.logger);

            loggingExecutorService.submitMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.decoratorBuilder)
                    .methodName(submit)
                    .startMessage("start " + submit)
                    .successMessage("finished " + submit)
                    .failureMessage("failed " + submit)
                    .build();
            loggingExecutorService.executeMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.decoratorBuilder)
                    .methodName(execute)
                    .startMessage("start " + execute)
                    .successMessage("finished " + execute)
                    .failureMessage("failed " + execute)
                    .build();

            return infere(loggingExecutorService);
        }
        //endregion

        //region Fields
        private LoggingExecutorService loggingExecutorService;
        protected LoggingSyncMethodDecorator.Builder<Object> decoratorBuilder;
        //endregion
    }
}
