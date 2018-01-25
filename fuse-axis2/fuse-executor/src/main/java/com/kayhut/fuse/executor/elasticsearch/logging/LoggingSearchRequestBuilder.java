package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
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
            LogMessage startMessage,
            LogMessage successMessage,
            LogMessage failureMessage,
            Timer timer,
            Meter successMeter,
            Meter failureMeter) {
        super(client, action);

        this.startMessage = startMessage;
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
        Timer.Context timerContext = this.timer.time();

        try {
            this.startMessage.log();
            return new LoggingActionFuture<>(
                    super.execute(),
                    this.successMessage,
                    this.failureMessage,
                    timerContext,
                    this.successMeter,
                    this.failureMeter);
        } catch (Exception ex) {
            this.failureMessage.with(ex).log();
            this.failureMeter.mark();
            timerContext.stop();
            throw ex;
        }
    }


    //endregion

    //region Fields
    private LogMessage startMessage;
    private LogMessage successMessage;
    private LogMessage failureMessage;
    private Timer timer;
    private Meter successMeter;
    private Meter failureMeter;
    //endregion
}
