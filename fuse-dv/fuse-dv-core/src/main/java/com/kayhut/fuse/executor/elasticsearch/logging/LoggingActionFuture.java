package com.kayhut.fuse.executor.elasticsearch.logging;

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

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 02/01/2018.
 */
public class LoggingActionFuture<T> implements ListenableActionFuture<T> {
    //region Constructors
    public LoggingActionFuture(
            ActionFuture<T> actionFuture,
            LogMessage successMessage,
            LogMessage failureMessage,
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
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.actionGet();
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(String s) {
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.actionGet(s);
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(long l) {
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.actionGet(l);
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(long l, TimeUnit timeUnit) {
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.actionGet(l, timeUnit);
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T actionGet(TimeValue timeValue) {
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.actionGet(timeValue);
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

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
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.get();
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

            try {
                this.timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean thrownExcepion = false;

        try {
            T response = actionFuture.get(timeout, unit);
            callListenersOnResponse(response);
            return response;
        } catch (Exception ex) {
            thrownExcepion = true;
            callListenersOnFailure(ex);
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }

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
    private LogMessage successMessage;
    private LogMessage failureMessage;
    private Closeable timerContext;
    private Meter successMeter;
    private Meter failureMeter;

    private Iterable<LogMessage.MDCWriter> finalWriters;

    private List<ActionListener<T>> listeners;
    //endregion
}
