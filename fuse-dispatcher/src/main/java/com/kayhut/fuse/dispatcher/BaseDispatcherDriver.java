package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.*;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.process.QueryMetadata;
import com.kayhut.fuse.model.transport.Response;

import java.util.UUID;

import static com.kayhut.fuse.model.Utils.readJsonFile;
import static com.kayhut.fuse.model.Utils.submit;
import static com.kayhut.fuse.model.process.ProcessElement.ProcessContext.set;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseDispatcherDriver implements DispatcherDriver {

    private EventBus eventBus;

    @Inject
    public BaseDispatcherDriver(EventBus eventBus) {
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
    public QueryData process(QueryData input) {
        set(new Response("In-Process"));
        return submit(eventBus, new QueryData(input.getMetadata()));
    }

    /**
     * fuse query proccess starts here
     *
     * @param input
     * @return
     */
    @Override
    @Subscribe
    public Response response(GtaData input) {
        //todo build result according to (input.getMetadata().getType()==["path" | "graph])
        QueryMetadata metadata = input.getMetadata();
        Content content;
        //case of graph build graph response
        if(input.getMetadata().getType().equals("graph")) {
            content = Graph.GraphBuilder.builder(metadata.getId())
                    .data(readJsonFile("result.json"))
                    .url("/result")
                    .compose();
        } else {
            //case of path build path response
            content = Path.PathBuilder.builder(metadata.getId())
                    .data(readJsonFile("result.json"))
                    .url("/result")
                    .compose();
        }
        //compose final response
        Response response = Response.ResponseBuilder.builder(UUID.randomUUID().toString())
                .metadata(metadata)
                .data(content)
                .compose();
        set(response);
        return response;
    }

}
