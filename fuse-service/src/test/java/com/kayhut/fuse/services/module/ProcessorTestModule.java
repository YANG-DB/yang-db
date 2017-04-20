package com.kayhut.fuse.services.module;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.services.dispatcher.context.processor.QueryCursorPageTestProcessor;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by Roman on 04/04/2017.
 */
public class ProcessorTestModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(QueryCursorPageTestProcessor.class).asEagerSingleton();
    }

}
