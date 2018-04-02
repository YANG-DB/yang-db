package com.kayhut.fuse.neo4j;

/*import org.neo4j.driver.v1.*;

import javax.inject.Inject;

/**
 * Created by liorp on 3/16/2017.
 */
/*public class SimpleGraphProvider implements GraphProvider {

    private GraphConfig config;

    @Inject
    public SimpleGraphProvider(GraphConfig config) {
        this.config = config;
    }

    @Override
    public Session getSession() {
        //Driver driver = GraphDatabase.driver(NEO4J_BOLT_URL, AuthTokens.basic(NEO4J_USER, NEO4J_PWD));
        Driver driver = GraphDatabase.driver(config.getboltUrl(), AuthTokens.basic(config.getUser(), config.getPwd()));
        Session session = driver.session();
        return session;
    }
}*/
