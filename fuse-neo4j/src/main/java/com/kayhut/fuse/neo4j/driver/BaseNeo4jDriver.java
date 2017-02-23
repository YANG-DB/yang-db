package com.kayhut.fuse.neo4j.driver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.ProcessElement;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.process.QueryMetadata;
import com.kayhut.fuse.neo4j.Neo4jDriver;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by EladW on 22/02/2017.
 */
@Singleton
public class BaseNeo4jDriver implements ProcessElement<QueryData,GtaData>, Neo4jDriver {

    private EventBus eventBus;

    static String NEO4J_BOLT_URL = "bolt://localhost:7687";

    static String NEO4J_USER = "neo4j";

    static String NEO4J_PWD = "1234";

    @Inject
    public BaseNeo4jDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public GtaData process(QueryData input) {
        //TODO: [TEMP] build a QueryMetadata object containing the results inside its 'type' field
        return submit(eventBus,new GtaData(new QueryMetadata(input.getMetadata().getId(),
                                                                input.getMetadata().getName(),
                                                                query(input.getMetadata().getType()),
                                                                input.getMetadata().getTime())));
    }

    public String query(String cypherQuery) {

        StringBuilder result = new StringBuilder();

        Driver driver = GraphDatabase.driver(NEO4J_BOLT_URL, AuthTokens.basic( NEO4J_USER, NEO4J_PWD ) );

        Session session = driver.session();

        try ( Transaction tx = session.beginTransaction() )
        {
            StatementResult res = tx.run( cypherQuery );
            tx.success();
            return res.toString();
        }
        catch (Exception e){

            return "Neo4j Query failed. reason: " + e.getMessage();

        }

    }

}
