package com.kayhut.fuse.executor.elasticsearch.logging;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingSearchScrollRequestBuilder extends SearchScrollRequestBuilder {
    //region Constructors
    public LoggingSearchScrollRequestBuilder(
            ElasticsearchClient client,
            SearchScrollAction action,
            String scrollId,
            LogMessage startMessage,
            LogMessage verboseMessage,
            LogMessage successMessage,
            LogMessage failureMessage,
            Timer timer,
            Meter successMeter,
            Meter failureMeter) {
        super(client, action, scrollId);

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
//        Composite.of()

        try {
            this.startMessage.log();
            ElasticQuery.logQuery(this.toString()).write();
            this.verboseMessage.with(this.toString()).log();
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
    private LogMessage startMessage;
    private LogMessage verboseMessage;
    private LogMessage successMessage;
    private LogMessage failureMessage;
    private Timer timer;
    private Meter successMeter;
    private Meter failureMeter;
    //endregion
}
