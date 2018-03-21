package com.kayhut.fuse.dispatcher.modules;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class DefaultIdGenModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<IdGeneratorDriver<Object>>(){}).toInstance(new IdGeneratorDriver<Object>() {
            @Override
            public Object getNext(String genName, int numIds) {
                return null;
            }
        });
    }
}
