package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingSearchRequestBuilder extends SearchRequestBuilder{
    //region Constructors
    public LoggingSearchRequestBuilder(
            ElasticsearchClient client,
            SearchAction action,
            Timer timer,
            Meter successMeter,
            Meter failureMeter,
            LogMessage startMessage,
            LogMessage successMessage,
            LogMessage failureMessage) {
        super(client, action);

        this.timer = timer;
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
        this.startMessage = startMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }
    //endregion

    //region Override Methods
    @Override
    public ActionFuture<SearchResponse> execute() {
        Timer.Context timerContext = this.timer.time();

        try {
            this.startMessage.log();
            ActionFuture<SearchResponse> future = super.execute();
            return new LoggingActionFuture<>(
                    future,
                    timerContext,
                    this.successMeter,
                    this.failureMeter,
                    this.successMessage,
                    this.failureMessage);
        } catch (Exception ex) {
            this.failureMeter.mark();
            this.failureMessage.with(ex).log();
            throw ex;
        }
    }


    //endregion

    //region Fields
    private Timer timer;
    private Meter successMeter;
    private Meter failureMeter;
    private LogMessage startMessage;
    private LogMessage successMessage;
    private LogMessage failureMessage;
    //endregion
}
