package com.kayhut.fuse.gta.strategy.promise;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategyTest {
    public static AsgQuery simpleQuery0(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped).build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void testOptions_none_entity1() throws Exception {
        AsgQuery query = simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get())
        );

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.Builder.get()
                                    .withEType("1").withName("Person").build()
                    );
                }
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));
        when(context.getGraphTraversalSource()).thenReturn(new PromiseGraph().traversal());

        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().V().as("A")
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Person")));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_none_entity1_rel2_entity3() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(2), context);
        GraphTraversal expectedTraversal = __.start().otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_filterEntity_entity1_rel2_entity3() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.Builder.get()
                                    .withEType("2").withName("Person").build()
                    );
                }
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.filterEntity);
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(2), context);
        GraphTraversal expectedTraversal = __.start().otherV()
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Person")))
                .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_none_entity1_rel2_filter10_entity3() throws Exception {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(3), context);
        GraphTraversal expectedTraversal = __.start().otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
