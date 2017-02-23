package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.Content;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.Path;
import com.kayhut.fuse.model.process.ExecutionContext;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.Response;

import java.util.UUID;

import static com.kayhut.fuse.model.process.ProcessElement.ProcessContext.set;

/**
 * Created by lior on 23/02/2017.
 */
public abstract class BaseDispatcherDriver implements DispatcherDriver<ExecutionContext>{
    /**
     * fuse query proccess starts here
     *
     * @param input
     * @return
     */
    @Override
    @Subscribe
    public Response wrap(ExecutionContext input) {
        //todo build result according to (input.getQueryMetadata().getType()==["path" | "graph])
        QueryMetadata metadata = input.getQueryMetadata();
        Content content;
        //case of graph build graph response
        if(input.getQueryMetadata().getType().equals("graph")) {
            content = Graph.GraphBuilder.builder(metadata.getId())
                    .data(new QueryResult())
//                    .data(readJsonFile("result.json"))
                    .compose();
        } else {
            //case of path build path response
            content = Path.PathBuilder.builder(metadata.getId())
                    .data(new QueryResult())
//                    .data(readJsonFile("result.json"))
                    .compose();
        }
        //compose final response
        Response response = Response.ResponseBuilder.builder(UUID.randomUUID().toString())
                .queryMetadata(metadata)
                .data(content)
                .compose();
        //As the flow ends -> setting the final response
        set(response);
        return response;
    }

}
