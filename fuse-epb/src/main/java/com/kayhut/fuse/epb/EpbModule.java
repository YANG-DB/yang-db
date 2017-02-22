package com.kayhut.fuse.epb;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class EpbModule implements Jooby.Module  {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(EpbDriver.class).to(BaseEpbDriver.class).asEagerSingleton();

    }
}
