package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.AsgData;
import com.kayhut.fuse.model.process.QueryData;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class DispatcherDriver  {

    private EventBus eventBus;

    @Inject
    public DispatcherDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    public QueryData process(QueryData input) {
        return submit(eventBus,new QueryData());
    }

}
