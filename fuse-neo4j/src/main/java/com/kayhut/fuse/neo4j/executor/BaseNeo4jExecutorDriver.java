package com.kayhut.fuse.neo4j.executor;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.process.ProcessElement;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.command.CursorCommand;
import com.kayhut.fuse.model.process.command.ExecutionCompleteCommand;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.Pattern;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.neo4j.cypher.Compiler;
import com.kayhut.fuse.neo4j.cypher.Schema;
import org.neo4j.driver.v1.*;

import java.io.File;
import java.util.Scanner;
import java.util.UUID;

import static com.kayhut.fuse.model.Utils.asString;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class BaseNeo4jExecutorDriver implements ProcessElement<QueryCursorData, ExecutionCompleteCommand>, Neo4jExecutorDriver {
    static String NEO4J_BOLT_URL = "bolt://localhost:7687";

    static String NEO4J_USER = "neo4j";

    static String NEO4J_PWD = "1234";

    private EventBus eventBus;

    @Inject
    public BaseNeo4jExecutorDriver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    @Subscribe
    public ExecutionCompleteCommand process(QueryCursorData input) {
        Query query = input.getQuery();
        try {
            String queryStr = asString(query);
            String result = query(queryStr);

            QueryResult resultGraph = new QueryResult();
            Pattern pattern = new Pattern();
            pattern.setName(result);
            resultGraph.setPattern(pattern);

            QueryMetadata queryMetadata = input.getQueryMetadata();
            ContentResponse response = ContentResponse.ResponseBuilder.builder(queryMetadata.getId())
                    .queryMetadata(queryMetadata)
                    .resultMetadata(input.getResultMetadata())
                    .data(Graph.GraphBuilder.builder(UUID.randomUUID().toString())
                            .data(resultGraph)
                            .compose())
                    .compose();
            return submit(eventBus, new ExecutionCompleteCommand(response));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ExecutionCompleteCommand();
    }


    @Subscribe
    public ExecutionCompleteCommand processCursor(CursorCommand input) {
        return submit(eventBus, new ExecutionCompleteCommand());
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
