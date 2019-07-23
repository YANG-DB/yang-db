package com.yangdb.fuse.executor.elasticsearch.logging;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.yangdb.fuse.dispatcher.logging.LogMessage;
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
