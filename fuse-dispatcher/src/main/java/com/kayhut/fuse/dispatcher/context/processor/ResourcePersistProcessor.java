package com.kayhut.fuse.dispatcher.context.processor;

import com.codahale.metrics.Slf4jReporter;
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
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;

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
    @LoggerAnnotation(name = "process", logLevel = Slf4jReporter.LoggingLevel.INFO)
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        //last step in creation of QueryCreationOperationContext => we can save the context now to the resourceStore
        if (context.getExecutionPlan() == null) {
            return context;
        }

        //store as query resource
        resourceStore.addQueryResource(new QueryResource(
                context.getQuery(),
                context.getAsgQuery(),
                context.getQueryMetadata(),
                context.getExecutionPlan()));

        return context.complete();
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    @LoggerAnnotation(name = "process", logLevel = Slf4jReporter.LoggingLevel.INFO)
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() == null) {
            return context;
        }

        context.getQueryResource().addCursorResource(context.getCursorId(),
                new CursorResource(context.getCursorId(), context.getCursor(), context.getCursorType()));
        return context.complete();
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    @LoggerAnnotation(name = "process", logLevel = Slf4jReporter.LoggingLevel.INFO)
    public PageCreationOperationContext process(PageCreationOperationContext context) {
        if (context.getPageResource() == null) {
            return context;
        }

        context.getCursorResource().addPageResource(context.getPageId(), context.getPageResource());
        return context.complete();
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private ResourceStore resourceStore;
    //endregion
}
