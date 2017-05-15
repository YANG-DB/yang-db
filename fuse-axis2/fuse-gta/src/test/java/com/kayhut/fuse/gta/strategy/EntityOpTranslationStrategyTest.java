package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategyTest {
    @Test
    public void testOptions_none_entity1() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get())
        );

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.none);

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(1).withName("Person").build()
                    );
                }
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOntology()).thenReturn(ontology);
        when(context.getGraphTraversalSource()).thenReturn(new PromiseGraph().traversal());

        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().V().as("A")
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Person")));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_none_entity1_rel2_entity3() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(query, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 3).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.none);
        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(2), context);
        GraphTraversal expectedTraversal = __.start().otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_filterEntity_entity1_rel2_entity3() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(query, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 3).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(2).withName("Person").build()
                    );
                }
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOntology()).thenReturn(ontology);

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.filterEntity);
        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(2), context);
        GraphTraversal expectedTraversal = __.start().otherV().as("B")
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Person")))
                .otherV();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_none_entity1_rel2_filter10_entity3() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(query, 2).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(query, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 3).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityOpTranslationStrategy.Options.none);
        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(3), context);
        GraphTraversal expectedTraversal = __.start().otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
