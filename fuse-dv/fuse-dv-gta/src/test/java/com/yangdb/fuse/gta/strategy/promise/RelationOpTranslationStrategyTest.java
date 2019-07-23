package com.yangdb.fuse.gta.strategy.promise;

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.OntologyTestUtils.DRAGON;
import com.yangdb.fuse.model.OntologyTestUtils.PERSON;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.unipop.controller.promise.GlobalConstants;
import com.yangdb.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static com.yangdb.fuse.model.OntologyTestUtils.FIRE;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class RelationOpTranslationStrategyTest {
    Ontology ontology = OntologyTestUtils.createDragonsOntologyShort();

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(rel(2,FIRE.getrType(),R))
                .next(typed(3, DRAGON.type,"B")).build();
    }

    @Test
    public void test_entity1_rel2_entity3() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );


        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationOpTranslationStrategy strategy = new RelationOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(1), context);

        GraphTraversal expectedTraversal = __.start().outE(GlobalConstants.Labels.PROMISE).as("A-->B")
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, FIRE.getName()),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_entity3_rel2_entity1() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get()),
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(query, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationOpTranslationStrategy strategy = new RelationOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(1), context);

        GraphTraversal expectedTraversal = __.start().outE(GlobalConstants.Labels.PROMISE).as("B-->A")
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, FIRE.getName()),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.IN))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}