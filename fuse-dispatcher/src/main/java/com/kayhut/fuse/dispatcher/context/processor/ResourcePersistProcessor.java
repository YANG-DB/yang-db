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
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;

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
        //last pattern in creation of QueryCreationOperationContext => we can save the context now to the resourceStore
        if (context.getExecutionPlan() == null) {
            return context;
        }

        this.resourceStore.addQueryResource(new QueryResource(
                context.getQuery(),
                context.getAsgQuery(),
                context.getQueryMetadata(),
                context.getExecutionPlan(),
                context.getPlanNode()));

        return context.complete();
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() == null) {
            return context;
        }

        this.resourceStore.addCursorResource(context.getQueryId(),
                new CursorResource(context.getCursorId(), context.getCursor(), context.getCursorType()));

        return context.complete();
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) {
        if (context.getPageResource() == null) {
            return context;
        }

        this.resourceStore.addPageResource(context.getQueryId(), context.getCursorId(), context.getPageResource());

        return context.complete();
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private ResourceStore resourceStore;
    //endregion
}
