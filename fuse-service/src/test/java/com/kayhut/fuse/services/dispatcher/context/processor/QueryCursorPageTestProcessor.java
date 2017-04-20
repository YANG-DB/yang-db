package com.kayhut.fuse.services.dispatcher.context.processor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.results.QueryResult;

import java.io.IOException;
import java.util.Collections;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by Roman on 04/04/2017.
 */
public class QueryCursorPageTestProcessor implements
        QueryCreationOperationContext.Processor,
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor {
    //region Constructors
    @Inject
    public QueryCursorPageTestProcessor(EventBus eventBus, CursorFactory cursorFactory) {
        this.eventBus = eventBus;
        this.cursorFactory = cursorFactory;
        this.eventBus.register(this);
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        if (context.getAsgQuery() != null && context.getExecutionPlan() == null) {
            context = context.of((Plan) Plan.PlanBuilder.build(Collections.emptyList()).compose());
            submit(eventBus, context);
        }

        return context;
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() == null) {
            context = context.of(cursorFactory.createCursor(context::getQueryResource));
            submit(eventBus, context);
        }

        return context;
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) throws IOException {
        if (context.getPageResource() == null) {
            QueryResult queryResult = context.getCursorResource().getCursor().getNextResults(context.getPageSize());
            context = context.of(new PageResource(context.getPageId(), queryResult, context.getPageSize()));
            submit(eventBus, context);
        }

        return context;
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private CursorFactory cursorFactory;
    //endregion
}
