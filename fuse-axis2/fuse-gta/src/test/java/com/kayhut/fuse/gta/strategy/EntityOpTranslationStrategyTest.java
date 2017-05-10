package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategyTest {
    @Test
    public void test_entity1() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get())
        );

        EntityOpTranslationStrategy entityOpTranslationStrategy = new EntityOpTranslationStrategy(new PromiseGraph());

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(1).withName("Person").build()
                    );
                }
        );

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getOntology()).thenReturn(ontology);
        when(context.getPlan()).thenReturn(plan);
        when(context.getPlanOp()).thenReturn(plan.getOps().get(0));

        GraphTraversal actualTraversal = entityOpTranslationStrategy.apply(context, __.start());
        GraphTraversal expectedTraversal = __.start().V().as("A")
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Person")));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_entity1_rel2_entity3() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(query, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 3).get())
        );

        EntityOpTranslationStrategy entityOpTranslationStrategy = new EntityOpTranslationStrategy(new PromiseGraph());

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(context.getPlanOp()).thenAnswer(invocationOnMock -> plan.getOps().get(2));

        GraphTraversal actualTraversal = entityOpTranslationStrategy.apply(context, __.start());
        GraphTraversal expectedTraversal = __.start().otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
