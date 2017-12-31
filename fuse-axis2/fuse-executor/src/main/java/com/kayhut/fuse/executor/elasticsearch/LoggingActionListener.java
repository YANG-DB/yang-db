package com.kayhut.fuse.executor.elasticsearch;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionListener;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingActionListener<TResponse> implements ActionListener<TResponse> {
    //region Constructors
    public LoggingActionListener(LogMessage successMessage, LogMessage failureMessage) {
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.innerActionListener = Optional.empty();
        this.innerFailureMessage = Optional.empty();
    }

    public LoggingActionListener(
            LogMessage successMessage,
            LogMessage failureMessage,
            ActionListener<TResponse> innerActionListener,
            LogMessage innerFailureMessage) {
        this(successMessage, failureMessage);
        this.innerActionListener = Optional.ofNullable(innerActionListener);
        this.innerFailureMessage = Optional.ofNullable(innerFailureMessage);
    }
    //endregion

    //region ActionListsner Implementation
    @Override
    public void onResponse(TResponse tResponse) {
        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onResponse(tResponse));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.with(ex).log());
        } finally {
            this.successMessage.log();
        }
    }

    @Override
    public void onFailure(Exception e) {
        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onFailure(e));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.with(ex).log());
        } finally {
            this.failureMessage.with(e).log();
        }
    }
    //endregion

    //region Fields
    private LogMessage successMessage;
    private LogMessage failureMessage;
    private Optional<ActionListener<TResponse>> innerActionListener;
    private Optional<LogMessage> innerFailureMessage;
    //endregion
}