package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.results.ResultMetadata;

import static com.kayhut.fuse.model.Utils.baseUrl;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseQueryDispatcherDriver  extends BaseDispatcherDriver implements QueryDispatcherDriver {
    private EventBus eventBus;

    @Inject
    public BaseQueryDispatcherDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }


    /**
     * fuse query proccess starts here
     *
     * @param input
     * @return
     */
    @Override
    public QueryCursorData process(QueryData input) {
        //As the flow starts -> setting the initial response
        String id = input.getQueryMetadata().getId();
        //build response metadata
        ResultMetadata resultMetadata = ResultMetadata.ResultMetadataBuilder.build(id)
                .cursorUrl(baseUrl() + "/query/" + id)
                .resultUrl(baseUrl() + "/query/" + id + "/result")
                .compose();

        return submit(eventBus, new QueryCursorData(id,input.getQueryMetadata(),input.getQuery(),resultMetadata));
    }
}
