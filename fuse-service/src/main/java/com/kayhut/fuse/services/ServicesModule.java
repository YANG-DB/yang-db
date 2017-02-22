package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.kayhut.fuse.asg.AsgDriver;
import com.kayhut.fuse.asg.BaseAsgDriver;
import com.kayhut.fuse.dispatcher.BaseDispatcherDriver;
import com.kayhut.fuse.dispatcher.DispatcherDriver;
import com.kayhut.fuse.epb.BaseEpbDriver;
import com.kayhut.fuse.epb.EpbDriver;
import com.kayhut.fuse.events.DeadEventsListener;
import com.kayhut.fuse.gta.BaseGtaDriver;
import com.kayhut.fuse.gta.GtaDriver;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class ServicesModule implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //dispatcher
        binder.bind(DispatcherDriver.class).to(BaseDispatcherDriver.class).asEagerSingleton();

        //service controllers
        binder.bind(QueryController.class).to(SimpleQueryController.class).asEagerSingleton();
        binder.bind(ResultsController.class).to(SimpleResultsController.class).asEagerSingleton();
        binder.bind(SearchController.class).to(SimpleSearchController.class).asEagerSingleton();
    }

}
