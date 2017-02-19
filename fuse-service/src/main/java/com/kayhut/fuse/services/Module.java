package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 15/02/2017.
 */
public class Module implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //register eventBus with service life cycle
        binder.bind(EventBus.class).toInstance(new EventBus());
        binder.bind(QueryController.class).to(SimpleQueryController.class);
        binder.bind(ResultsController.class).to(SimpleResultsController.class);
        binder.bind(SearchController.class).to(SimpleSearchController.class);
    }

}
