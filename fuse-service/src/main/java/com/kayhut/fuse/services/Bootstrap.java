package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.kayhut.fuse.events.DeadEventsListener;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by lior on 22/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class Bootstrap implements Jooby.Module  {
    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //register eventBus with service life cycle
        binder.bind(EventBus.class).toInstance(new EventBus());
        binder.bind(DeadEventsListener.class).toInstance(new DeadEventsListener());
        //load modules according to configuration
        loadModules(env,conf,binder);

    }

    private void loadModules(Env env, Config conf, Binder binder) {
        List<String> modules = conf.getStringList("modules");
        modules.forEach(value -> {
            try {
                Method method = Jooby.Module.class.getMethod("configure",Env.class,Config.class,Binder.class);
                method.invoke(Class.forName(value).newInstance(),env,conf,binder);
            } catch (Exception e) {
                //todo something usefull here - sbould the app break ???
                e.printStackTrace();
            }
        });
    }
}
