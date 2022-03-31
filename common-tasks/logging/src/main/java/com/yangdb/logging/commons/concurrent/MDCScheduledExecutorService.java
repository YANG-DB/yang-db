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

import com.yangdb.logging.slf4j.ExtendedMDC;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MDCScheduledExecutorService extends MDCExecutorService implements ScheduledExecutorService {
    //region Constructors
    public MDCScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }
    //endregion

    //region ScheduledExecutorService Implementation
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.scheduledExecutorService.schedule(() -> {
                    MDC.setContextMap(mdc);
                    ExtendedMDC.setContextMap(extendedMDC);
                    command.run();
                },
                delay,
                unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.scheduledExecutorService.schedule(() -> {
                    MDC.setContextMap(mdc);
                    ExtendedMDC.setContextMap(extendedMDC);
                    return callable.call();
                },
                delay,
                unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.scheduledExecutorService.scheduleAtFixedRate(() -> {
                    MDC.setContextMap(mdc);
                    ExtendedMDC.setContextMap(extendedMDC);
                    command.run();
                },
                initialDelay,
                period,
                unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        Map<String, Object> extendedMDC = ExtendedMDC.getCopyOfContextMap();
        return this.scheduledExecutorService.scheduleAtFixedRate(() -> {
                    MDC.setContextMap(mdc);
                    ExtendedMDC.setContextMap(extendedMDC);
                    command.run();
                },
                initialDelay,
                delay,
                unit);
    }
    //endregion

    //region Fields
    private ScheduledExecutorService scheduledExecutorService;
    //endregion
}
