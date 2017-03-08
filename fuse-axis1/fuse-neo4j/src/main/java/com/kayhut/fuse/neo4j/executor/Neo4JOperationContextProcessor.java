package com.kayhut.fuse.neo4j.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.results.*;
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
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by User on 08/03/2017.
 */
public class Neo4JOperationContextProcessor implements
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor {
    //region Constructors
    @Inject
    public Neo4JOperationContextProcessor(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() != null) {
            return context;
        }

        // TODO: use ASG
        // AsgQuery asgQuery = input.getAsgQuery();

        return submit(eventBus, context.of(new Neo4jCursor(context.getQueryResource().getQuery())));
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) {
        if (context.getPageResource() != null) {
            return context;
        }

        Neo4jCursor neo4jCursor = (Neo4jCursor)context.getCursorResource().getCursor();
        Query query = neo4jCursor.getQuery();

        QueryResult result = query(query);
        if (result == null) {
            result = new QueryResult();
        }

        return submit(eventBus, context.of(result));
    }
    //endregion

    //region Private Methods
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
    //endregion

    //region Fields
    protected EventBus eventBus;

    static String NEO4J_BOLT_URL = "bolt://localhost:7687";
    static String NEO4J_USER = "neo4j";
    static String NEO4J_PWD = "1234";
    //endregion
}
