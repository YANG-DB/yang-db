package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionListener;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingActionListener<TResponse> implements ActionListener<TResponse> {
    //region Constructors
    public LoggingActionListener(
            LogMessage successMessage,
            LogMessage failureMessage,
            Timer.Context timerContext,
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
            LogMessage successMessage,
            LogMessage failureMessage,
            LogMessage innerFailureMessage,
            Timer.Context timerContext,
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
        timerContext.stop();

        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onResponse(tResponse));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.with(ex).log());
        } finally {
            this.successMessage.log();
            this.successMeter.mark();
        }
    }

    @Override
    public void onFailure(Exception e) {
        timerContext.stop();

        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onFailure(e));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.with(ex).log());
        } finally {
            this.failureMessage.with(e).log();
            this.failureMeter.mark();
        }
    }
    //endregion

    //region Fields
    private LogMessage successMessage;
    private LogMessage failureMessage;
    private Timer.Context timerContext;
    private Meter successMeter;
    private Meter failureMeter;
    private Optional<ActionListener<TResponse>> innerActionListener;
    private Optional<LogMessage> innerFailureMessage;
    //endregion
}