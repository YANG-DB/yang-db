package com.kayhut.fuse.executor;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.ProcessElement;
import com.kayhut.fuse.dispatcher.context.QueryExecutionContext;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseExecutorDriver implements ProcessElement {
    private EventBus eventBus;

    @Inject
    public BaseExecutorDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public QueryExecutionContext process(QueryExecutionContext input) {
        return null;
    }
}
