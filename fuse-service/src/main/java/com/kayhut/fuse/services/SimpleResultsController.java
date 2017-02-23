package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.events.ExecutionCompleteEvent;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;
import javaslang.Tuple2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleResultsController implements ResultsController {
    //map of cursor id -> map of result id -> results (path/graph)
    private final Map<String, Map<String,Tuple2<Request,Response>>> map = new ConcurrentHashMap<>();

    @Inject
    public SimpleResultsController(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void observe(ExecutionCompleteEvent event) {
        String cursorId = event.getRequest().getId();
        String resultId = event.getResponse().getResultMetadata().getId();
        map.getOrDefault(cursorId,new HashMap<>()).putIfAbsent(resultId,new Tuple2<>(event.getRequest(),event.getResponse()));
    }

    @Override
    public Response get(String cursorId, String resultId) {
        if(!map.containsKey(cursorId))
            return new Response("CursorId["+cursorId+"] Not Found");
        //return cached result
        Map<String,Tuple2<Request,Response>> map = this.map.getOrDefault(cursorId, Collections.EMPTY_MAP);
        return map.getOrDefault(resultId,new Tuple2<Request,Response>(null,null))._2();
    }
}
