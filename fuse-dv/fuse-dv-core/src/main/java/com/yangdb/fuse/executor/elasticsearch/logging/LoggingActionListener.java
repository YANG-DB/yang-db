package com.yangdb.fuse.executor.elasticsearch.logging;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.codahale.metrics.Meter;
import com.yangdb.fuse.dispatcher.logging.LogMessage;
import org.opensearch.action.ActionListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingActionListener<TResponse> implements ActionListener<TResponse> {
    //region Constructors
    public LoggingActionListener(
            Function<TResponse, LogMessage> successMessage,
            Function<Exception, LogMessage> failureMessage,
            Closeable timerContext,
            Meter successMeter,
            Meter failureMeter) {
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.innerActionListener = Optional.empty();
        this.innerFailureMessage = Optional.empty();
        this.timerContext = timerContext;
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
    }

    public LoggingActionListener(
            ActionListener<TResponse> innerActionListener,
           Function<TResponse, LogMessage> successMessage,
            Function<Exception, LogMessage> failureMessage,
            Function<Exception, LogMessage> innerFailureMessage,
            Closeable timerContext,
            Meter successMeter,
            Meter failureMeter) {
        this(successMessage, failureMessage, timerContext, successMeter, failureMeter);
        this.innerActionListener = Optional.ofNullable(innerActionListener);
        this.innerFailureMessage = Optional.ofNullable(innerFailureMessage);
    }
    //endregion

    //region ActionListsner Implementation
    @Override
    public void onResponse(TResponse tResponse) {
        try {
            this.timerContext.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onResponse(tResponse));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.apply(ex).log());
        } finally {
            this.successMessage.apply(tResponse).log();
            this.successMeter.mark();
        }
    }

    @Override
    public void onFailure(Exception e) {
        try {
            this.timerContext.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onFailure(e));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.apply(ex).log());
        } finally {
            this.failureMessage.apply(e).log();
            this.failureMeter.mark();
        }
    }
    //endregion

    //region Fields
    private Function<TResponse, LogMessage> successMessage;
    private Function<Exception, LogMessage> failureMessage;
    private Closeable timerContext;
    private Meter successMeter;
    private Meter failureMeter;
    private Optional<ActionListener<TResponse>> innerActionListener;
    private Optional<Function<Exception, LogMessage>> innerFailureMessage;
    //endregion
}
