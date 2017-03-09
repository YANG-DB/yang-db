package com.kayhut.fuse.dispatcher.context.processor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;

/**
 * Created by User on 08/03/2017.
 */
@Singleton
public class ResourcePersistProcessor implements
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor,
        QueryCreationOperationContext.Processor {
    //region Constructors
    @Inject
    public ResourcePersistProcessor(EventBus eventBus, ResourceStore resourceStore) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.resourceStore = resourceStore;
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        if (context.getAsgQuery() == null || context.isComplete()) {
            return context;
        }

        resourceStore.addQueryResource(new QueryResource(context.getQuery(), context.getQueryMetadata()));

        context = context.complete();
        return context;
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() == null) {
            return context;
        }

        context.getQueryResource().addCursorResource(context.getCursorId(),
                new CursorResource(context.getCursorId(), context.getCursor(), context.getCursorType()));
        context = context.complete();
        return context;
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) {
        if (context.getPageResource() == null) {
            return context;
        }

        context.getCursorResource().addPageResource(context.getPageId(), context.getPageResource());
        context = context.complete();
        return context;
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private ResourceStore resourceStore;
    //endregion
}
