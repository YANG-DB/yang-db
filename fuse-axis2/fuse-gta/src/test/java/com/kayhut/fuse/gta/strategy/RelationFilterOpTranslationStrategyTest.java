package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class RelationFilterOpTranslationStrategyTest {
    @Test
    public void test_rel2_filter10() {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtils.<Rel>getElement(query, 2).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(query, 10).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType(2);
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            return Collections.singletonList(timestampProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                plan,
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                            __.has(T.label, "Fire"),
                            __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT),
                            __.has("timestamp", P.gt(10)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_reversed_rel2_filter10() {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtils.reverseRelation(AsgQueryUtils.<Rel>getElement(query, 2).get())),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(query, 10).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType(2);
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            return Collections.singletonList(timestampProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                plan,
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.IN),
                                __.has("timestamp", P.gt(10)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
