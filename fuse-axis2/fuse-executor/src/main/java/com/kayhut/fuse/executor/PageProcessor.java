package com.kayhut.fuse.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.model.results.QueryResult;

import java.io.IOException;

import static com.kayhut.fuse.model.Utils.*;

/**
 * Created by liorp on 3/16/2017.
 */
public class PageProcessor implements PageCreationOperationContext.Processor {
    public static final String DRAGONS_RESULTS = "results/results";

    private final EventBus eventBus;

    @Inject
    public PageProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) throws IOException {
        if (context.getPageResource() != null) {
            return context;
        }
        QueryResult result = asObject(readJsonFile(DRAGONS_RESULTS), QueryResult.class);
        return submit(eventBus, context.of(new PageResource(context.getPageId(), result, context.getPageSize())));
    }
}
