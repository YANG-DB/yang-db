package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

/**
 * Created by lior on 23/02/2017.
 */
@Singleton
public class UnipopDispatcherDriver extends BaseQueryDispatcherDriver {

    @Inject
    public UnipopDispatcherDriver(Config conf,EventBus eventBus) {
        super(conf,eventBus);
    }

}
