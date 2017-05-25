package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.extenders.PushDownSplitFilterPlanExtensionStrategy;
import com.kayhut.fuse.executor.ontology.GraphLayoutProviderFactory;
import com.kayhut.fuse.executor.ontology.PhysicalIndexProviderFactory;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.OntologyTestUtils.DRAGON;
import com.kayhut.fuse.model.OntologyTestUtils.PERSON;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.PushdownRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

public class PushDownStrategyPlanGeneratorExtenderStrategyTest {
    OntologyProvider ontologyProvider;
    PhysicalIndexProviderFactory physicalIndexProviderFactory;
    GraphLayoutProviderFactory graphLayoutProviderFactory;

    @Before
    public void setup(){
        ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        GraphLayoutProvider graphLayoutProvider = ((edgeType, property) -> {
                if(property.getName().equals("firstName"))
                    return Optional.of(new GraphRedundantPropertySchema.Impl(property.getName(), "entityB.firstName", property.getType()));
                if(property.getName().equals("gender"))
                    return Optional.of(new GraphRedundantPropertySchema.Impl(property.getName(), "entityB.gender", property.getType()));
                if(property.getName().equals("id"))
                    return Optional.of(new GraphRedundantPropertySchema.Impl(property.getName(), "entityB.id", property.getType()));
                if(property.getName().equals("type"))
                    return Optional.of(new GraphRedundantPropertySchema.Impl(property.getName(), "entityB.type", property.getType()));

                return Optional.empty();
            });

        graphLayoutProviderFactory = ontology -> graphLayoutProvider;
        physicalIndexProviderFactory = (ontology -> new PhysicalIndexProvider.Constant(new StaticIndexPartition(Arrays.asList("index"))));
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

        PushDownSplitFilterPlanExtensionStrategy strategy = new PushDownSplitFilterPlanExtensionStrategy(
                ontologyProvider,
                physicalIndexProviderFactory,
                graphLayoutProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        assertEquals(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().size(),3);
        //first eProp is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().get(1) instanceof PushdownRelProp);
        Optional<RelProp> idRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().stream().
                filter(r -> r instanceof PushdownRelProp && ((PushdownRelProp) r).getPushdownPropName().equals("entityB.id")).findFirst();
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

        PushDownSplitFilterPlanExtensionStrategy strategy = new PushDownSplitFilterPlanExtensionStrategy(
                ontologyProvider,
                physicalIndexProviderFactory,
                graphLayoutProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);

        assertEquals(PlanUtil.first$(extendedPlans.get(0), EntityFilterOp.class).getAsgEBase().geteBase().getProps().size(),1);
        assertEquals(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().size(),3);

        //first eProp is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().get(1) instanceof PushdownRelProp);
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().get(2) instanceof PushdownRelProp);

        Optional<RelProp> firstNameRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().stream().
                filter(r -> r instanceof PushdownRelProp && ((PushdownRelProp) r).getPushdownPropName().equals("entityB.firstName")).findFirst();
        Assert.assertTrue(firstNameRelProp.isPresent());
        Optional<RelProp> typeRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEBase().geteBase().getProps().stream().
                filter(r -> r instanceof PushdownRelProp && ((PushdownRelProp) r).getPushdownPropName().equals("entityB.type")).findFirst();
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
                .next(typed(3,  2))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(5, 4, R)
                                .next(unTyped( 6))
                        , rel(7, 5, R)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete(8, "concrete1", 3, "Concrete1", "D"))
                )
                .build();
    }
    //endregion
}
