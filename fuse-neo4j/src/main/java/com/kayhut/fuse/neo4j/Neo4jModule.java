package com.kayhut.fuse.neo4j;

import com.kayhut.fuse.neo4j.driver.BaseNeo4jDriver;
import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by User on 22/02/2017.
 */
public class Neo4jModule implements Jooby.Module  {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(Neo4jDriver.class).to(BaseNeo4jDriver.class).asEagerSingleton();
    }
}
