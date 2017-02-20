package com.kayhut.fuse.epb;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.AsgData;
import com.kayhut.fuse.model.process.EpbData;
import com.kayhut.fuse.model.process.ProcessElement;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class EpbDriver implements ProcessElement<AsgData,EpbData> {
    private EventBus eventBus;

    @Inject
    public EpbDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public EpbData process(AsgData input) {
        return submit(eventBus,new EpbData());
    }

}
