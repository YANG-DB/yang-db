package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.transport.ContentResponse;

import java.util.Optional;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleResultsController implements ResultsController {
    private final ResourceStore resourceStore;

    @Inject
    public SimpleResultsController(
            EventBus eventBus,
            ResourceStore resourceStore) {
        this.resourceStore = resourceStore;
        eventBus.register(this);
    }

    @Override
    public ContentResponse get(String queryId, int cursorId, int resultId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);

        if(!queryResource.isPresent()) {
            return new ContentResponse("QueryId[" + queryId + "] Not Found");
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursor(cursorId);
        if (!cursorResource.isPresent()) {
            return new ContentResponse("CursorId[" + cursorId + "] Not Found");
        }

        Optional<ContentResponse> result = cursorResource.get().getResultResource(resultId);
        if (!result.isPresent()) {
            return new ContentResponse("ResultId[" + resultId + "] Not Found");
        }


        return result.get();
    }
}
