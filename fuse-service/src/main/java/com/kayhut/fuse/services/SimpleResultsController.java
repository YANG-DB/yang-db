package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.events.ExecutionCompleteEvent;
import com.kayhut.fuse.model.Content;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;
import javaslang.Tuple2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleResultsController implements ResultsController {
    private final Map<String, Tuple2<Request,Content>> map = new ConcurrentHashMap<>();

    @Inject
    public SimpleResultsController(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void observe(ExecutionCompleteEvent event) {
        map.put(event.getRequest().getId(),new Tuple2<>(event.getRequest(),event.getData()));

    }
    @Override
    public Response get(String id) {
        if(!map.containsKey(id))
            return new Response(id,"Not Found",null);
        //return cached result
        return new Response(id,map.get(id)._1.getName(),map.get(id)._2());
    }
}
