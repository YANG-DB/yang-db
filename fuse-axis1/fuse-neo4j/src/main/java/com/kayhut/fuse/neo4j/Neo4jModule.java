package com.kayhut.fuse.neo4j;

import com.google.inject.Binder;
import com.kayhut.fuse.neo4j.executor.BaseNeo4jExecutorDriver;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by User on 23/02/2017.
 */
public class Neo4jModule implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //service controllers
        binder.bind(BaseNeo4jExecutorDriver.class).asEagerSingleton();
    }

}
