package com.kayhut.fuse.epb;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.ProcessElement;
import com.kayhut.fuse.dispatcher.context.QueryExecutionContext;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseEpbDriver implements ProcessElement {
    private EventBus eventBus;

    @Inject
    public BaseEpbDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    public QueryExecutionContext process(QueryExecutionContext input) {
        return null;
    }
}
