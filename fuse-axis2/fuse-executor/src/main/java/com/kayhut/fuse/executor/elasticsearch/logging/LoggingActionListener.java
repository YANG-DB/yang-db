package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionListener;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingActionListener<TResponse> implements ActionListener<TResponse> {
    //region Constructors
    public LoggingActionListener(
            LogMessage successMessage,
            LogMessage failureMessage,
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
            LogMessage successMessage,
            LogMessage failureMessage,
            LogMessage innerFailureMessage,
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
            innerFailureMessage.ifPresent(logMessage -> logMessage.with(ex).log());
        } finally {
            this.successMessage.log();
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
    private Closeable timerContext;
    private Meter successMeter;
    private Meter failureMeter;
    private Optional<ActionListener<TResponse>> innerActionListener;
    private Optional<LogMessage> innerFailureMessage;
    //endregion
}