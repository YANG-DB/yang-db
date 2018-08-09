package com.kayhut.fuse.gta.translation.discrete;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.gta.strategy.discrete.M1PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;

import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.FuseGraphTraversalSource;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import com.kayhut.fuse.unipop.structure.FuseUniGraph;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.unipop.process.strategyregistrar.StrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1ChainedPlanOpTraversalTranslatorTest {
    UniGraphProvider uniGraphProvider;
    PlanTraversalTranslator translator;

    @Before
    public void setUp() throws Exception {
        translator = new ChainedPlanOpTraversalTranslator(new M1PlanOpTranslationStrategy());
        UniGraph uniGraph = new FuseUniGraph(null, graph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return Collections.emptySet();
            }

            @Override
            public void close() {

            }
        }, DefaultTraversalStrategies::new);

        this.uniGraphProvider = mock(UniGraphProvider.class);
        when(uniGraphProvider.getGraph(any())).thenReturn(uniGraph);
    }

    @Test
    public void test_concrete_rel_untyped() throws Exception {
        Plan plan = create_Con_Rel_Unt_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, this.uniGraphProvider.getGraph(ont.get()).traversal()));

        Traversal expectedTraversal =
                __.start().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_typed() throws Exception {
        Plan plan = create_Con_Rel_Typ_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_typed_rel_concrete() throws Exception {
        Plan plan = create_Typ_Rel_Con_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Dragon")))
                        .outE().as("B-->A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("A");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_typed_rel_typed() throws Exception {
        Plan plan = create_Typ_Rel_Typ_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Person")))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_typed_rel_untyped() throws Exception {
        Plan plan = create_Con_Rel_Typ_Rel_Unt_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B")
                        .outE().as("B-->C")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("C");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    //region Building Plans
    private Plan create_Typ_Rel_Con_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(typed(1, "2", "B"))
                .next(quant1(2, QuantType.all))
                .in(eProp(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(concrete(6, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(7, QuantType.all))
                .next(eProp(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Typ_Rel_Typ_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(typed(1, "1", "A"))
                .next(quant1(2, QuantType.all))
                .in(eProp(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(typed(6, "2", "B"))
                .next(quant1(7, QuantType.all))
                .next(eProp(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Typ_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(eProp(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(typed(6, "1", "B"))
                .next(quant1(7, QuantType.all))
                .next(eProp(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Unt_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(eProp(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(unTyped(6, "B"))
                .next(quant1(7, QuantType.all))
                .next(eProp(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Typ_Rel_Unt_PathQuery()
    {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(eProp(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(typed(6, "2", "B"))
                .next(quant1(7, QuantType.all))
                .in(eProp(8))
                .next(rel(9, "1", Rel.Direction.R).below(relProp(10)))
                .next(unTyped(11, "C"))
                .next(quant1(12, QuantType.all))
                .next(eProp(13))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(AsgQueryUtil.element$(query, 9)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 10)),
                new EntityOp(AsgQueryUtil.element$(query, 11)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 13))
        );
    }
    //endregion


    private Ontology.Accessor getOntologyAccessor() {
        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("1").withName("Person").build());
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("2").withName("Dragon").build());
                    return  entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );

        return new Ontology.Accessor(ontology);
    }
}
