package com.kayhut.fuse.asg;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.AsgData;
import com.kayhut.fuse.model.process.ProcessElement;
import com.kayhut.fuse.model.process.QueryData;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class AsgDriver implements ProcessElement<QueryData,AsgData> {
    private EventBus eventBus;

    @Inject
    public AsgDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Subscribe
    public AsgData process(QueryData input) {
        return submit(eventBus,new AsgData());
    }

}
