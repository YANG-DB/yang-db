package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.UnionOpTranslationStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.UnionOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.FuseGraphTraversalSource;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import com.kayhut.fuse.unipop.structure.FuseUniGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.mockito.Mockito.when;

@Ignore("Still under construction")
public class UnionOpTranslationStrategyTest {

    public static AsgQuery simpleQuery0(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "B"))
                .next(quant1(2, some))
                .in(ePropGroup(9,
                        EProp.of(9, "1", of(eq, "value1")),
                        EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void testUnion_none() throws Exception {
        AsgQuery query = simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new UnionOp(AsgQueryUtil.element$(query, 2))
        );

        PlanOpTranslationStrategy strategy = new UnionOpTranslationStrategy(new M2PlanOpTranslationStrategy());

        Ontology ontology = Ontology.OntologyBuilder.anOntology().withEntityTypes(
                Collections.singletonList(
                        EntityType.Builder.get().withEType("1").withName("Person").build()
                )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));
        when(context.getGraphTraversalSource()).thenReturn(
                new FuseGraphTraversalSource(new FuseUniGraph(null, graph -> new ControllerManager() {
                    @Override
                    public Set<UniQueryController> getControllers() {
                        return Collections.emptySet();
                    }

                    @Override
                    public void close() {
                    }
                }, DefaultTraversalStrategies::new)));

        GraphTraversal actualTraversal = strategy.translate(__.start().V(), new PlanWithCost<>(plan, null), plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().V().union();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    @Ignore("fix expected")
    public void testUnion_One_Branches_One_Hierarchy() throws Exception {
        AsgQuery query = simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new UnionOp(AsgQueryUtil.element$(query, 2),
                        new Plan(
                                new EntityOp(AsgQueryUtil.element$(query, 1)),
                                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                                new RelationOp(AsgQueryUtil.element$(query, 5)),
                                new EntityOp(AsgQueryUtil.element$(query, 6))
                        ).getOps())
        );

        PlanOpTranslationStrategy strategy = new UnionOpTranslationStrategy(new M2PlanOpTranslationStrategy());

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withRelationshipTypes(
                        Arrays.asList(
                                RelationshipType.Builder.get().withRType("4").withName("Has").build(),
                                RelationshipType.Builder.get().withRType("5").withName("was").build())
                )
                .withEntityTypes(
                        Collections.singletonList(
                                EntityType.Builder.get().withEType("1").withName("Person").build()
                        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));
        when(context.getGraphTraversalSource()).thenReturn(
                new FuseGraphTraversalSource(new FuseUniGraph(null, graph -> new ControllerManager() {
                    @Override
                    public Set<UniQueryController> getControllers() {
                        return Collections.emptySet();
                    }

                    @Override
                    public void close() {
                    }
                }, DefaultTraversalStrategies::new)));

        GraphTraversal actualTraversal = strategy.translate(__.start().V(), new PlanWithCost<>(plan, null), plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().V().union(
                __.start().has(T.label, "Fire")
        );

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    @Ignore("fix expected")
    public void testUnion_Two_Branches_One_Hierarchy() throws Exception {
        AsgQuery query = simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new UnionOp(AsgQueryUtil.element$(query, 2),
                        new Plan(
                                new EntityOp(AsgQueryUtil.element$(query, 1)),
                                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                                new RelationOp(AsgQueryUtil.element$(query, 5)),
                                new EntityOp(AsgQueryUtil.element$(query, 6))
                        ).getOps(),
                        new Plan(
                                new EntityOp(AsgQueryUtil.element$(query, 1)),
                                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                                new EntityOp(AsgQueryUtil.element$(query, 8))
                        ).getOps())
        );

        PlanOpTranslationStrategy strategy = new UnionOpTranslationStrategy(new M2PlanOpTranslationStrategy());

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withRelationshipTypes(
                        Arrays.asList(
                                RelationshipType.Builder.get().withRType("4").withName("Has").build(),
                                RelationshipType.Builder.get().withRType("5").withName("was").build())
                )
                .withEntityTypes(
                        Collections.singletonList(
                                EntityType.Builder.get().withEType("1").withName("Person").build()
                        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));
        when(context.getGraphTraversalSource()).thenReturn(
                new FuseGraphTraversalSource(new FuseUniGraph(null, graph -> new ControllerManager() {
                    @Override
                    public Set<UniQueryController> getControllers() {
                        return Collections.emptySet();
                    }

                    @Override
                    public void close() {
                    }
                }, DefaultTraversalStrategies::new)));

        GraphTraversal actualTraversal = strategy.translate(__.start().V(), new PlanWithCost<>(plan, null), plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().V().union(
                __.start().has(T.label, "Fire")
        );

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }


}