package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.results.ResultMetadata;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.kayhut.fuse.model.Utils.baseUrl;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseQueryDispatcherDriver  extends BaseDispatcherDriver implements QueryDispatcherDriver {
    //todo verify the sequencer works
    private ConcurrentHashMap<String,Integer> sequence;
    private EventBus eventBus;

    @Inject
    public BaseQueryDispatcherDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.sequence = new ConcurrentHashMap<>();
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
        //sequence.containsKey(id) ? sequence.put(id,Integer.valueOf(sequence.get(id))+1) : sequence.put(id,0);
        String sequence = UUID.randomUUID().toString();
        //build response metadata
        ResultMetadata resultMetadata = ResultMetadata.ResultMetadataBuilder.build(String.valueOf(sequence))
                .cursorUrl(baseUrl() + "/query/" + id)
                .resultUrl(baseUrl() + "/query/" + id + "/result/"+sequence)
                .compose();

        return submit(eventBus, new QueryCursorData(id,input.getQueryMetadata(),input.getQuery(),resultMetadata));
    }
}
