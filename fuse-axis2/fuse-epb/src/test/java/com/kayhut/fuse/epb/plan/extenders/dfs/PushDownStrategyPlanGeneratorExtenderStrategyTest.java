package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.extenders.PushDownSplitFilterPlanExtensionStrategy;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.properties.PushdownRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PushDownStrategyPlanGeneratorExtenderStrategyTest {
    OntologyProvider ontologyProvider;
    GraphElementSchemaProvider schemaProvider;

    @Before
    public void setup(){
        ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        GraphEdgeSchema edgeSchema = mock(GraphEdgeSchema.class);
        schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(edgeSchema));

        GraphEdgeSchema.End end = mock(GraphEdgeSchema.End.class);
        when(edgeSchema.getDestination()).thenReturn(Optional.of(end));

        GraphRedundantPropertySchema firstNamePropertySchema = mock(GraphRedundantPropertySchema.class);
        when(firstNamePropertySchema.getPropertyRedundantName()).thenReturn("entityB.firstName");
        GraphRedundantPropertySchema genderPropertySchema = mock(GraphRedundantPropertySchema.class);
        when(genderPropertySchema.getPropertyRedundantName()).thenReturn("entityB.gender");
        GraphRedundantPropertySchema idPropertySchema = mock(GraphRedundantPropertySchema.class);
        when(idPropertySchema.getPropertyRedundantName()).thenReturn("entityB.id");
        GraphRedundantPropertySchema typePropertySchema = mock(GraphRedundantPropertySchema.class);
        when(typePropertySchema.getPropertyRedundantName()).thenReturn("entityB.type");
        when(end.getRedundantVertexProperty(any())).thenAnswer(invocationOnMock -> {
            String prop = invocationOnMock.getArgumentAt(0, String.class);
            if(prop.equals("firstName"))
                return Optional.of(firstNamePropertySchema);
            if(prop.equals("gender"))
                return Optional.empty();
            if(prop.equals("id"))
                return Optional.of(idPropertySchema);
            if(prop.equals("type"))
                return Optional.of(typePropertySchema);
            return Optional.empty();
        });
    }

    @Test
    public void test_simpleQuery2RedundantFilterSplitPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        PushDownSplitFilterPlanExtensionStrategy strategy = new PushDownSplitFilterPlanExtensionStrategy(ontologyProvider, schemaProvider);
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);

        assertEquals(PlanUtil.findFirst$(extendedPlans.get(0), EntityFilterOp.class).getAsgEBase().geteBase().getProps().size(),1);
        assertEquals(PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().size(),3);

        //first eProp is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().get(1) instanceof PushdownRelProp);
        assertTrue(PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().get(2) instanceof PushdownRelProp);

        Optional<RelProp> firstNameRelProp = PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().stream().
                filter(r -> r instanceof PushdownRelProp && ((PushdownRelProp) r).getPushdownPropName().equals("entityB.firstName")).findFirst();
        Assert.assertTrue(firstNameRelProp.isPresent());
        Optional<RelProp> typeRelProp = PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().stream().
                filter(r -> r instanceof PushdownRelProp && ((PushdownRelProp) r).getPushdownPropName().equals("entityB.type")).findFirst();
        Assert.assertTrue(typeRelProp.isPresent());
        Assert.assertEquals("Dragon",((List<String>)typeRelProp.get().getCon().getExpr()).get(0));
    }

    @Test
    public void test_EConcreteRedundantFilterSplitPlan() {
        AsgQuery asgQuery = AsgQueryStore.queryWithEtypedAndEconcrete("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        PushDownSplitFilterPlanExtensionStrategy strategy = new PushDownSplitFilterPlanExtensionStrategy(ontologyProvider,schemaProvider);
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        assertEquals(PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().size(),3);
        //first eProp is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().get(1) instanceof PushdownRelProp);
        Optional<RelProp> idRelProp = PlanUtil.findFirst$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().stream().
                filter(r -> r instanceof PushdownRelProp && ((PushdownRelProp) r).getPushdownPropName().equals("entityB.id")).findFirst();
        Assert.assertTrue(idRelProp.isPresent());
        Assert.assertEquals("123",idRelProp.get().getCon().getExpr());
    }
}
