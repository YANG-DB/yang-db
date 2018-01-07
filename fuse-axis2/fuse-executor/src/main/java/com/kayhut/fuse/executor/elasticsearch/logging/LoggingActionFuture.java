package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexController;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.common.unit.TimeValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 02/01/2018.
 */
public class LoggingActionFuture<T> implements ActionFuture<T> {
    //region Constructors
    public LoggingActionFuture(
            ActionFuture<T> actionFuture,
            LogMessage successMessage,
            LogMessage failureMessage,
            Timer.Context timerContext,
            Meter successMeter,
            Meter failureMeter) {
        this.actionFuture = actionFuture;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.timerContext = timerContext;
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
    }
    //endregion

    //region ActionFuture Implementation
    @Override
    public T actionGet() {
        boolean thrownExcepion = false;

        try {
            return actionFuture.actionGet();
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
        }
    }

    @Override
    public T actionGet(String s) {
        boolean thrownExcepion = false;

        try {
            return actionFuture.actionGet(s);
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
        }
    }

    @Override
    public T actionGet(long l) {
        boolean thrownExcepion = false;

        try {
            return actionFuture.actionGet(l);
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
        }
    }

    @Override
    public T actionGet(long l, TimeUnit timeUnit) {
        boolean thrownExcepion = false;

        try {
            return actionFuture.actionGet(l, timeUnit);
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
        }
    }

    @Override
    public T actionGet(TimeValue timeValue) {
        boolean thrownExcepion = false;

        try {
            return actionFuture.actionGet(timeValue);
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
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
            return actionFuture.get();
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean thrownExcepion = false;

        try {
            return actionFuture.get(timeout, unit);
        } catch (Exception ex) {
            thrownExcepion = true;
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            throw ex;
        } finally {
            if (!thrownExcepion) {
                this.successMessage.log();
                this.successMeter.mark();
            }
            this.timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private ActionFuture<T> actionFuture;
    private LogMessage successMessage;
    private LogMessage failureMessage;
    private Timer.Context timerContext;
    private Meter successMeter;
    private Meter failureMeter;
    //endregion
}
