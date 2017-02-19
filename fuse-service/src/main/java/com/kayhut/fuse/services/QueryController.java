package com.kayhut.fuse.services;

import com.google.inject.Singleton;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.Plan;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class QueryController implements Query {
    @PostConstruct
    public void start() {
        // ...
    }

    @Override
    public Response query(Request request) {
        return new Response(UUID.randomUUID().toString(),request.getName(),new Graph());
    }

    @Override
    public Response plan(Request request) {
        return new Response(UUID.randomUUID().toString(),request.getName(),new Plan());
    }
}
