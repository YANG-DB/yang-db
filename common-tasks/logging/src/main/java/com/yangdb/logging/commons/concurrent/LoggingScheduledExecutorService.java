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
import com.yangdb.logging.LoggingSyncMethodDecorator;
import com.yangdb.logging.mdc.MethodName;
import org.slf4j.Logger;

import java.util.concurrent.*;

import static com.yangdb.commons.util.GenericUtils.infere;

public class LoggingScheduledExecutorService extends LoggingExecutorService implements ScheduledExecutorService {
    //region Constructors
    public LoggingScheduledExecutorService(ScheduledExecutorService scheduledExecutorService, Logger logger) {
        super(scheduledExecutorService, logger);
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduleMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(logger, schedule).build();
    }
    //endregion

    //region ScheduledExecutorService Implementation
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return this.scheduledExecutorService.schedule(() -> {
                    try {
                        this.scheduleMethodDecorator.decorate(() -> {
                            command.run();
                            return null;
                        });
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                delay,
                unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return this.scheduledExecutorService.schedule(() -> {
                    try {
                        return infere(this.scheduleMethodDecorator.decorate(callable::call));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                delay,
                unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return this.scheduledExecutorService.scheduleAtFixedRate(() -> {
                    try {
                        this.scheduleMethodDecorator.decorate(() -> {
                            command.run();
                            return null;
                        });
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                initialDelay,
                period,
                unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
                    try {
                        this.scheduleMethodDecorator.decorate(() -> {
                            command.run();
                            return null;
                        });
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                initialDelay,
                delay,
                unit);
    }
    //endregion

    //region Fields
    private ScheduledExecutorService scheduledExecutorService;
    private LoggingSyncMethodDecorator<Object> scheduleMethodDecorator;

    private static MethodName.Value schedule = MethodName.of("schedule");
    //endregion

    public static class Builder extends LoggingExecutorService.Builder implements GenericBuilder<ExecutorService> {
        public Builder(ScheduledExecutorService executorService, Logger logger) {
            super(executorService, logger);
            this.loggingScheduledExecutorService = new LoggingScheduledExecutorService(executorService, logger);
        }

        //region GenericBuilder Implementation
        @Override
        public <T2 extends ExecutorService> T2 build() {
            LoggingScheduledExecutorService loggingScheduledExecutorService = new LoggingScheduledExecutorService(
                    this.loggingScheduledExecutorService.scheduledExecutorService,
                    this.loggingScheduledExecutorService.logger
            );

            loggingScheduledExecutorService.submitMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.decoratorBuilder)
                    .methodName(submit)
                    .startMessage("start " + submit)
                    .successMessage("finished " + submit)
                    .failureMessage("failed " + submit)
                    .build();
            loggingScheduledExecutorService.executeMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.decoratorBuilder)
                    .methodName(execute)
                    .startMessage("start " + execute)
                    .successMessage("finished " + execute)
                    .failureMessage("failed " + execute)
                    .build();

            loggingScheduledExecutorService.scheduleMethodDecorator = new LoggingSyncMethodDecorator.Builder<>(this.decoratorBuilder)
                    .methodName(schedule)
                    .startMessage("start " + schedule)
                    .successMessage("finished " + schedule)
                    .failureMessage("failed " + schedule)
                    .build();

            return infere(loggingScheduledExecutorService);
        }
        //endregion

        //region Fields
        private LoggingScheduledExecutorService loggingScheduledExecutorService;
        //endregion
    }
}
