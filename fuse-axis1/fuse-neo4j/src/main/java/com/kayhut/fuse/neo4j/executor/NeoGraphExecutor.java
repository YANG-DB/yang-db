package com.kayhut.fuse.neo4j.executor;

import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.neo4j.GraphProvider;
import javaslang.Tuple2;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.helpers.collection.Iterators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liorp on 3/16/2017.
 */
abstract class NeoGraphUtils{

    //region Private Methods
    public static QueryResult query(GraphProvider graphProvider, Neo4jCursorFactory.Neo4jCursor cursor) {

        try {
            ArrayList<Assignment> assignments = new ArrayList<>();
            Tuple2<Transaction, StatementResult> resultTuple2 = graphProvider.run(cursor.getContext().getCypher());

            Iterators.asList(resultTuple2._2).forEach(record -> {
                //Each records represents an assignment (containing nodes and relationships)
                ArrayList<Entity> entities = new ArrayList<>();
                ArrayList<Relationship> rels = new ArrayList<>();
                List<Value> valueList = record.values().stream().collect(Collectors.toList());
                valueList.forEach(value -> {

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

                        Entity entity = Entity.Builder.instance()
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

                        Relationship rel = Relationship.Builder.instance()
                                .withAgg(false)
                                .withRID(String.valueOf(r.asRelationship().id()))
                                .withDirectional(true)
                                .withEID1(String.valueOf(r.asRelationship().startNodeId()))
                                .withEID2(String.valueOf(r.asRelationship().endNodeId()))
                                .withProperties(props).build();
                        rels.add(rel);
                    }

                });

                Assignment assignment = Assignment.Builder.instance()
                        .withEntities(entities)
                        .withRelationships(rels)
                        .build();

                assignments.add(assignment);

            });
            resultTuple2._1.success();

            return QueryResult.Builder.instance().withAssignments(assignments).build();

        } catch (Exception e) {
            //throw new RuntimeException(e);
            return null;
        }
    }
}
