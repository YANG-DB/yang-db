package com.kayhut.fuse.executor.elasticsearch;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.slf4j.Logger;

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
            LogMessage failureMessage) {
        super(client, action);

        this.startMessage = startMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }
    //endregion

    //region Override Methods
    @Override
    public ActionFuture<SearchResponse> execute() {
        try {
            this.startMessage.log();
            ActionFuture<SearchResponse> future = super.execute();
            return new LoggingActionFuture<>(future, this.successMessage, this.failureMessage);
        } catch (Exception ex) {
            this.failureMessage.with(ex).log();
            throw ex;
        }
    }
    //endregion

    //region Fields
    private LogMessage startMessage;
    private LogMessage successMessage;
    private LogMessage failureMessage;
    //endregion
}
