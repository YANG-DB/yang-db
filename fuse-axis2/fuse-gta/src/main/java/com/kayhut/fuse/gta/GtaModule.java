package com.kayhut.fuse.gta;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.unipop.controller.UniGraphProvider;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class GtaModule implements Jooby.Module  {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(UniGraphProvider.class).asEagerSingleton();
        binder.bind(GremlinTranslationAppenderEngine.class).asEagerSingleton();
        binder.bind(CursorCreationOperationContext.Processor.class).to(GtaCursorProcessor.class).asEagerSingleton();
    }
}
