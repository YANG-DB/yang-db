package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.OntologyTestUtils.DRAGON;
import com.kayhut.fuse.model.OntologyTestUtils.PERSON;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RedundantRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RedundantStrategyPlanGeneratorExtenderStrategyTest {
    @Before
    public void setup(){
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }

    //region Test Methods
    @Test
    public void test_EConcreteRedundantFilterSplitPlan() {
        AsgQuery asgQuery = query1();

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        RedundantFilterPlanExtensionStrategy strategy = new RedundantFilterPlanExtensionStrategy(this.ontologyProvider, this.schemaProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        assertEquals(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().size(),3);
        //first eProp is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().get(1) instanceof RedundantRelProp);
        Optional<RelProp> idRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().stream().
                filter(r -> r instanceof RedundantRelProp && ((RedundantRelProp) r).getRedundantPropName().equals("entityB.id")).findFirst();
        Assert.assertTrue(idRelProp.isPresent());
        Assert.assertEquals("123",idRelProp.get().getCon().getExpr());
    }

    @Test
    public void test_simpleQuery2RedundantFilterSplitPlan() {
        AsgQuery asgQuery = query2();

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        RedundantFilterPlanExtensionStrategy strategy = new RedundantFilterPlanExtensionStrategy(this.ontologyProvider, this.schemaProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);

        assertEquals(0,PlanUtil.first$(extendedPlans.get(0), EntityFilterOp.class).getAsgEbase().geteBase().getProps().size());
        assertEquals(4,PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().size());

        //first eProp is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().get(1) instanceof RedundantRelProp);
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().get(2) instanceof RedundantRelProp);

        Optional<RelProp> firstNameRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().stream().
                filter(r -> r instanceof RedundantRelProp && ((RedundantRelProp) r).getRedundantPropName().equals("entityB.firstName")).findFirst();
        Assert.assertTrue(firstNameRelProp.isPresent());
        Optional<RelProp> typeRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().stream().
                filter(r -> r instanceof RedundantRelProp && ((RedundantRelProp) r).getRedundantPropName().equals("entityB.type")).findFirst();
        Assert.assertTrue(typeRelProp.isPresent());
        Assert.assertEquals("Dragon",((List<String>)typeRelProp.get().getCon().getExpr()).get(0));
    }
    //endregion

    //region Private Methods
    private AsgQuery query1() {
        return AsgQuery.Builder.start("name", "ont" )
                .next(typed(1, PERSON.type))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(10, RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "123", DRAGON.type, "B", "tag"))
                .build();
    }

    private AsgQuery query2() {
        return AsgQuery.Builder.start("name", "ont")
                .next(typed(1,  PERSON.type))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(10, RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(typed(3,  DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("firstName", 9, of(eq, "value1")), EProp.of("gender", 9, of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped( 6))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of("deathDate", 11, of(eq, "value5")), RelProp.of("birthDate", 11, of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                entity.geteType(),
                                new StaticIndexPartitions(Collections.singletonList("index"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                relation.getrType(),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityA.id"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityB.id"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("firstName", "entityB.firstName", ont.property$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("gender", "entityB.gender", ont.property$("gender").getType()),
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", ont.property$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", ont.property$("type").getType())
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Collections.singletonList("index"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
