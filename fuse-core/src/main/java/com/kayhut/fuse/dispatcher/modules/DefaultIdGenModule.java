package com.kayhut.fuse.dispatcher.modules;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class DefaultIdGenModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) {
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>(){}).toInstance((genName, numIds) -> new Range(1l,2l));
    }
}
