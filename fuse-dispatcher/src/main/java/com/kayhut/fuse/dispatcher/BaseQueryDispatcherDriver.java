package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.results.ResultMetadata;
import com.typesafe.config.Config;

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
    private Config conf;
    private EventBus eventBus;

    @Inject
    public BaseQueryDispatcherDriver(Config conf, EventBus eventBus) {
        this.conf = conf;
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
        String port = conf.getString("application.port");
        //As the flow starts -> setting the initial response
        String id = input.getQueryMetadata().getId();
        //sequence.containsKey(id) ? sequence.put(id,Integer.valueOf(sequence.get(id))+1) : sequence.put(id,0);
        String sequence = UUID.randomUUID().toString();
        //build response metadata
        String host = baseUrl(port);
        ResultMetadata resultMetadata = ResultMetadata.ResultMetadataBuilder.build(String.valueOf(sequence))
                .cursorUrl(host + "/query/" + id)
                .resultUrl(host + "/query/" + id + "/result/"+sequence)
                .compose();

        return submit(eventBus, new QueryCursorData(id,input.getQueryMetadata(),input.getQuery(),resultMetadata));
    }
}
