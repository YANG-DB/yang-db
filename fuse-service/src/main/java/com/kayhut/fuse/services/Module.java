package com.kayhut.fuse.services;

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
        binder.bind(Query.class).to(QueryController.class);
        binder.bind(Results.class).to(ResultsController.class);
    }
}
