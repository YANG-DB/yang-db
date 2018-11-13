package com.kayhut.fuse.executor.elasticsearch.logging;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexController;
import javaslang.collection.Stream;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.common.unit.TimeValue;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 02/01/2018.
 */
public class LoggingActionFuture<T> implements ListenableActionFuture<T> {
    //region Constructors
    public LoggingActionFuture(
            ActionFuture<T> actionFuture,
            Function<T, LogMessage> successMessage,
            Function<Exception, LogMessage> failureMessage,
            Closeable timerContext,
            Meter successMeter,
            Meter failureMeter) {
        this.actionFuture = actionFuture;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.timerContext = timerContext;
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;

        this.listeners = Collections.emptyList();
    }
    //endregion

    //region ActionFuture Implementation
    @Override
    public T actionGet() {
        try {
            T response = actionFuture.actionGet();
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(String s) {
        try {
            T response = actionFuture.actionGet(s);
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(long l) {
        try {
            T response = actionFuture.actionGet(l);
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(long l, TimeUnit timeUnit) {
        try {
            T response = actionFuture.actionGet(l, timeUnit);
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(TimeValue timeValue) {
        try {
            T response = actionFuture.actionGet(timeValue);
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return actionFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return actionFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return actionFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            T response = actionFuture.get();
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            T response = actionFuture.get(timeout, unit);
            callListenersOnResponse(response);
            this.successMessage.apply(response).log();
            this.successMeter.mark();
            return response;
        } catch (Exception ex) {
            callListenersOnFailure(ex);
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addListener(ActionListener<T> actionListener) {
        if (this.listeners.isEmpty()) {
            this.listeners = new ArrayList<>();
        }

        this.listeners.add(actionListener);
    }
    //endregion

    //region Private Methods
    private void callListenersOnResponse(T response) {
        for(ActionListener<T> actionListener : this.listeners) {
            actionListener.onResponse(response);
        }
    }

    private void callListenersOnFailure(Exception ex) {
        for(ActionListener<T> actionListener : this.listeners) {
            actionListener.onFailure(ex);
        }
    }
    //endregion

    //region Fields
    private ActionFuture<T> actionFuture;
    private Function<T, LogMessage> successMessage;
    private Function<Exception, LogMessage> failureMessage;
    private Closeable timerContext;
    private Meter successMeter;
    private Meter failureMeter;

    private List<ActionListener<T>> listeners;
    //endregion
}
