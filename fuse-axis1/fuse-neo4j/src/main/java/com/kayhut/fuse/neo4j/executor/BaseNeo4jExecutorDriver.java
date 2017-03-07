package com.kayhut.fuse.neo4j.executor;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.BaseCursorDispatcherDriver;
import com.kayhut.fuse.dispatcher.ProcessElement;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.context.CursorExecutionContext;
import com.kayhut.fuse.dispatcher.context.QueryExecutionContext;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.results.Pattern;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.neo4j.cypher.Compiler;
import com.kayhut.fuse.neo4j.cypher.Schema;
import com.typesafe.config.Config;
import org.neo4j.driver.v1.*;

import java.io.File;
import java.util.Scanner;

import static com.kayhut.fuse.model.Utils.asString;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseNeo4jExecutorDriver extends BaseCursorDispatcherDriver implements ProcessElement {
    static String NEO4J_BOLT_URL = "bolt://localhost:7687";

    static String NEO4J_USER = "neo4j";

    static String NEO4J_PWD = "1234";

    @Inject
    public BaseNeo4jExecutorDriver(Config conf, EventBus eventBus, ResourceStore resourceStore) {
        super(conf, eventBus, resourceStore);
    }



    @Subscribe
    public CursorExecutionContext process(CursorExecutionContext input) {
        Neo4jCursor neo4jCursor = (Neo4jCursor)input.getCursorResource().getCursor();
        String cypherQuery = neo4jCursor.getCypherQuery();

        String result = query(cypherQuery);

        QueryResult resultGraph = new QueryResult();
        Pattern pattern = new Pattern();
        pattern.setName(result);
        resultGraph.setPattern(pattern);

        return submit(eventBus, input.of(resultGraph));
    }

    @Override
    @Subscribe
    public QueryExecutionContext process(QueryExecutionContext input) {
        Query query = input.getQuery();
        try {
            String queryStr = asString(query);
            return submit(eventBus, input.of(new Neo4jCursor(queryStr)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String query(String query) {

        try {

            StringBuilder resultsBuilder = new StringBuilder();

            String ontology = new Scanner(new File("C:\\Elad\\Cypher\\dragon_ont.json")).useDelimiter("\\Z").next();

            Schema schema = new Schema();

            schema.load(ontology);

            Compiler compiler = new Compiler();

            String cypherQuery = compiler.compile(query, schema);

            Driver driver = GraphDatabase.driver(NEO4J_BOLT_URL, AuthTokens.basic(NEO4J_USER, NEO4J_PWD));

            Session session = driver.session();

            Transaction tx = session.beginTransaction();

            StatementResult res = tx.run(cypherQuery);

            while(res.hasNext()) {
                resultsBuilder.append(res.next());
            }

            tx.success();

            return resultsBuilder.toString();

        } catch (Exception e) {
            return "Neo4j Query failed. reason: " + e.getMessage();
        }

    }
}
