package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingSearchRequestBuilder extends SearchRequestBuilder {
    //region Constructors
    public LoggingSearchRequestBuilder(
            ElasticsearchClient client,
            SearchAction action,
            Function<SearchRequestBuilder, LogMessage> startMessage,
            Function<SearchRequestBuilder, LogMessage> verboseMessage,
            Function<SearchResponse, LogMessage> successMessage,
            Function<Exception, LogMessage> failureMessage,
            Timer timer,
            Meter successMeter,
            Meter failureMeter) {
        super(client, action);

        this.startMessage = startMessage;
        this.verboseMessage = verboseMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.timer = timer;
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
    }
    //endregion

    //region Override Methods
    @Override
    public ListenableActionFuture<SearchResponse> execute() {
        Closeable timerContext = this.timer.time();

        try {
            this.startMessage.apply(this).log();
            this.verboseMessage.apply(this).log();
            return new LoggingActionFuture<>(
                    super.execute(),
                    this.successMessage,
                    this.failureMessage,
                    timerContext,
                    this.successMeter,
                    this.failureMeter);
        } catch (Exception ex) {
            this.failureMessage.apply(ex).log();
            this.failureMeter.mark();

            try {
                timerContext.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            throw ex;
        }
    }


    //endregion

    //region Fields
    private Function<SearchRequestBuilder, LogMessage> startMessage;
    private Function<SearchRequestBuilder, LogMessage> verboseMessage;
    private Function<SearchResponse, LogMessage> successMessage;
    private Function<Exception, LogMessage> failureMessage;

    private Timer timer;
    private Meter successMeter;
    private Meter failureMeter;
    //endregion
}