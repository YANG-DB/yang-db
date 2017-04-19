package com.kayhut.fuse.dispatcher.context.processor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.model.results.QueryResult;

import java.io.IOException;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/16/2017.
 */
public class PageProcessor implements PageCreationOperationContext.Processor {
    //region Constructors
    @Inject
    public PageProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) throws IOException {
        if (context.getPageResource() != null && context.getPageResource().getData() != null) {
            return context;
        }

        Cursor cursor = context.getCursorResource().getCursor();
        QueryResult results = cursor.getNextResults(context.getPageSize());
        return submit(eventBus, context.of(new PageResource(context.getPageId(), results, context.getPageSize())
                .withActualSize(results.getAssignments().size())
                .available()));
    }
    //endregion

    //region Fields
    private final EventBus eventBus;
    //endregion
}
