package com.kayhut.fuse.gta;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.EpbData;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.ProcessElement;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseGtaDriver implements ProcessElement<EpbData,GtaData>, GtaDriver {
    private EventBus eventBus;

    @Inject
    public BaseGtaDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public GtaData process(EpbData input) {
        return submit(eventBus,new GtaData(input.getId(),input.getQueryMetadata(),input.getQuery(),input.getResultMetadata()));
    }
}
