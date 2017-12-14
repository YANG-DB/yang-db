package com.kayhut.fuse.executor.elasticsearch;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.slf4j.Logger;

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
            LogMessage successMessage,
            LogMessage failureMessage) {
        super(client, action, scrollId);

        this.startMessage = startMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }
    //endregion

    //region Override Methods
    @Override
    public ListenableActionFuture<SearchResponse> execute() {
        try {
            this.startMessage.log();
            ListenableActionFuture<SearchResponse> future = super.execute();
            future.addListener(new LoggingActionListener<>(this.successMessage, this.failureMessage));
            return future;
        } catch (Exception ex) {
            this.failureMessage.with(ex).log();
            return null;
        }
    }
    //endregion

    //region Fields
    private LogMessage startMessage;
    private LogMessage successMessage;
    private LogMessage failureMessage;
    //endregion
}
