package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.command.ExecutionCompleteCommand;
import com.kayhut.fuse.model.transport.ContentResponse;

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
    private final Map<String, Map<String,ContentResponse>> map = new ConcurrentHashMap<>();

    @Inject
    public SimpleResultsController(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void observe(ExecutionCompleteCommand event) {
        String cursorId = event.getResponse().getQueryMetadata().getId();
        String resultId = event.getResponse().getResultMetadata().getId();
        if(!map.containsKey(cursorId)) {
            map.put(cursorId,new HashMap<>());
        }
        map.get(cursorId).put(resultId,event.getResponse());
    }

    @Override
    public ContentResponse get(String cursorId, String resultId) {
        if(!map.containsKey(cursorId))
            return new ContentResponse("CursorId["+cursorId+"] Not Found");
        //return cached result
        Map<String,ContentResponse> map = this.map.getOrDefault(cursorId, Collections.EMPTY_MAP);
        return map.getOrDefault(resultId,new ContentResponse("Not-Found"));
    }
}
