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
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.Schema;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.v1.*;
import org.neo4j.helpers.collection.Iterators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

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

        QueryResult result = query(input.getQuery());

        QueryMetadata queryMetadata = input.getQueryMetadata();

        ContentResponse response = ContentResponse.ResponseBuilder.builder(queryMetadata.getId())
                .queryMetadata(queryMetadata)
                .resultMetadata(input.getResultMetadata())
                .data(Graph.GraphBuilder.builder(UUID.randomUUID().toString())
                        .data(result)
                        .compose())
                .compose();

        return submit(eventBus, new ExecutionCompleteCommand(response));
    }

    @Subscribe
    public ExecutionCompleteCommand processCursor(CursorCommand input) {
        return submit(eventBus, new ExecutionCompleteCommand());
    }

    public QueryResult query(Query query) {

        try {

            ArrayList<Assignment> assignments = new ArrayList<>();

            //TODO: get ontology from the ontology service
            String ontology = new Scanner(new File("C:\\Elad\\Cypher\\dragon_ont.json")).useDelimiter("\\Z").next();

            Schema schema = new Schema();

            schema.load(ontology);

            String cypherQuery = CypherCompiler.compile(query, schema);

            Driver driver = GraphDatabase.driver(NEO4J_BOLT_URL, AuthTokens.basic(NEO4J_USER, NEO4J_PWD));

            Session session = driver.session();

            Transaction tx = session.beginTransaction();

            StatementResult statementResult = tx.run(cypherQuery);

            Iterators.asList(statementResult).stream().forEach(record -> {

                //Each records represents an assignment (containing nodes and relationships)

                ArrayList<Entity> entities = new ArrayList<>();

                ArrayList<Relationship> rels = new ArrayList<>();

                List<Value> valueList = record.values().stream().collect(Collectors.toList());

                valueList.stream().forEach(value -> {

                    //Each value inside a record is either a node or a relationship

                    if(value instanceof NodeValue) {

                        NodeValue n = (NodeValue)value;

                        ArrayList<Property> props = new ArrayList<>();

                        n.asNode().keys().forEach(propName -> {
                            Property prop = new Property();
                            prop.setAgg(propName);
                            prop.setValue(String.valueOf(n.asNode().get(propName)));
                            props.add(prop);
                        });

                        Entity entity = Entity.EntityBuilder.anEntity()
                                            .withETag(Iterators.asList(n.asNode().labels().iterator()))
                                            .withProperties(props).build();

                        entities.add(entity);

                    }
                    else if(value instanceof RelationshipValue) {

                        RelationshipValue r = (RelationshipValue) value;

                        ArrayList<Property> props = new ArrayList<>();

                        r.asRelationship().keys().forEach(propName -> {
                            Property prop = new Property();
                            prop.setAgg(propName);
                            prop.setValue(String.valueOf(r.asRelationship().get(propName)));
                            props.add(prop);
                        });

                        Relationship rel = Relationship.RelationshipBuilder.aRelationship()
                                                    .withAgg(false)
                                                    .withRID(String.valueOf(r.asRelationship().id()))
                                                    .withDirectional(true)
                                                    .withEID1(String.valueOf(r.asRelationship().startNodeId()))
                                                    .withEID2(String.valueOf(r.asRelationship().endNodeId()))
                                                    .withProperties(props).build();

                        rels.add(rel);
                    }

                });

                Assignment assignment = Assignment.AssignmentBuilder.anAssignment()
                                                            .withEntities(entities)
                                                            .withRelationships(rels)
                                                            .build();

                assignments.add(assignment);

            });

            tx.success();

            return QueryResult.QueryResultBuilder.aQueryResult().withAssignments(assignments).build();

        } catch (Exception e) {
            return null;
        }

    }

}
