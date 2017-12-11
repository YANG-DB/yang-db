package com.kayhut.fuse.dispatcher.context.processor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.model.results.QueryResult;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/16/2017.
 */
public class PageProcessor implements PageCreationOperationContext.Processor {

    @Inject
    private MetricRegistry metricRegistry;

    //region Constructors
    @Inject
    public PageProcessor(ResourceStore resourceStore, EventBus eventBus) {
        this.resourceStore = resourceStore;

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

        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getCursorId())).time();



        Optional<CursorResource> cursorResource = this.resourceStore.getCursorResource(context.getQueryId(), context.getCursorId());
        if (!cursorResource.isPresent()) {
            // maybe cursor was already deleted - so throw an error
            return context;
        }

        QueryResult results = cursorResource.get().getCursor().getNextResults(context.getPageSize());

        long elapsedMillis = TimeUnit.MILLISECONDS.convert(time.stop(), TimeUnit.NANOSECONDS);
        return submit(eventBus, context.of(new PageResource(context.getPageId(), results, context.getPageSize(), elapsedMillis)
                .withActualSize(results.getAssignments().size())
                .available()));
    }
    //endregion

    //region Fields
    private final EventBus eventBus;
    private ResourceStore resourceStore;
    //endregion
}
