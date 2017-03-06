package com.kayhut.fuse.executor;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.ProcessElement;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseExecutorDriver implements ProcessElement, ExecutorDriver {
    private EventBus eventBus;

    @Inject
    public BaseExecutorDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public Object process(Object input) {
        return submit(eventBus,input);
    }

}
