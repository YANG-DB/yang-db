package com.kayhut.fuse.neo4j;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.neo4j.executor.Neo4jCursorFactory;
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
        //binder.bind(Neo4jOperationContextProcessor.class).asEagerSingleton();
        binder.bind(GraphProvider.class).to(SimpleGraphProvider.class).asEagerSingleton();
        binder.bind(CursorFactory.class).to(Neo4jCursorFactory.class).asEagerSingleton();

        Neo4JGraphConfig neo4JGraphConfig = new Neo4JGraphConfig(conf.getString("neo4j.bolt"),
                                                                 conf.getString("neo4j.user"),
                                                                 conf.getString("neo4j.password"));

        binder.bind(GraphConfig.class).toInstance(neo4JGraphConfig);
    }

}
