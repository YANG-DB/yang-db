package com.yangdb.fuse.epb.plan.estimation;

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProvider;
import com.yangdb.fuse.epb.utils.PlanMockUtils;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.yangdb.fuse.model.execution.plan.costs.DoubleCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;

import static com.google.common.collect.Lists.newArrayList;
import static com.yangdb.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator.getSupportedPattern;
import static com.yangdb.fuse.epb.utils.PlanMockUtils.Type.CONCRETE;
import static com.yangdb.fuse.epb.utils.PlanMockUtils.Type.TYPED;
import static com.yangdb.fuse.epb.utils.StatisticsMockUtils.build;
import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.execution.plan.Direction.out;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.RelProp.of;
import static com.yangdb.fuse.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 4/19/2017.
 */
public class StatisticalCostEstimatorTests {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(10, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, NAME.type, Constraint.of(eq, "smith")), EProp.of(9, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(11, START_DATE.type,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(11, END_DATE.type, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "smoge", DRAGON.type, "Display:smoge", "D"))
                )
                .build();
    }

    @Before
    public void setup() {
        graphElementSchemaProvider = mock(GraphElementSchemaProvider.class);
        GraphEdgeSchema graphEdgeSchema = mock(GraphEdgeSchema.class);
        GraphEdgeSchema.End edgeEnd = mock(GraphEdgeSchema.End.class);
        when(edgeEnd.getRedundantProperty(any())).thenAnswer(invocationOnMock -> {
            String property = (String) invocationOnMock.getArguments()[0];
            if (property.equals("lastName")) {
                return Optional.of(new GraphRedundantPropertySchema.Impl("lastName", "entityB.lastName", "string"));
            }


            return Optional.empty();


        });
        when(graphEdgeSchema.getEndB()).thenReturn(Optional.of(edgeEnd));
        when(graphElementSchemaProvider.getEdgeSchemas(any())).thenReturn(Collections.singletonList(graphEdgeSchema));
        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    }

    @Test
    public void planEstimatorPatternTwoTest() {

        RegexPatternCostEstimator.Pattern[] supportedPattern = getSupportedPattern();

        Plan plan1 = new Plan().withOp(new EntityOp());
        String s1 = IterablePlanOpDescriptor.getLight().describe(plan1.getOps());

        Plan plan2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp());
        String s2 = IterablePlanOpDescriptor.getLight().describe(plan2.getOps());

        Plan plan3 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationFilterOp());
        String s3 = IterablePlanOpDescriptor.getLight().describe(plan3.getOps());

        java.util.regex.Pattern compileP2 = java.util.regex.Pattern.compile(supportedPattern[1].pattern());

        Matcher matcher = compileP2.matcher(s1);
        if (matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOnly"), "EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOnlyFilter"), null);
        }

        matcher = compileP2.matcher(s2);
        if (matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOnly"), "EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOnlyFilter"), "EntityFilterOp");
        }

        matcher = compileP2.matcher(s3);
        Assert.assertFalse(matcher.matches());
    }

    @Test
    public void planEstimatorPatternOneTest() {
        RegexPatternCostEstimator.Pattern[] supportedPattern = getSupportedPattern();

        Plan plan1 = new Plan().withOp(new EntityOp());
        String s1 = IterablePlanOpDescriptor.getLight().describe(plan1.getOps());

        Plan plan2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp());
        String s2 = IterablePlanOpDescriptor.getLight().describe(plan2.getOps());

        Plan plan3 = new Plan().withOp(new EntityOp()).withOp(new RelationOp()).withOp(new EntityOp());
        String s3 = IterablePlanOpDescriptor.getLight().describe(plan3.getOps());

        Plan plan4 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationOp()).withOp(new RelationFilterOp()).withOp(new EntityOp()).withOp(new EntityFilterOp());
        String s4 = IterablePlanOpDescriptor.getLight().describe(plan4.getOps());


        java.util.regex.Pattern compileP1 = java.util.regex.Pattern.compile(supportedPattern[0].pattern());

        Matcher matcher = compileP1.matcher(s1);
        Assert.assertFalse(matcher.matches());

        matcher = compileP1.matcher(s2);
        Assert.assertFalse(matcher.matches());

        matcher = compileP1.matcher(s3);
        if (matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOne"), "EntityOp");
            Assert.assertEquals(matcher.group("relation"), "RelationOp");
            Assert.assertEquals(matcher.group("entityTwo"), "EntityOp");
        }

        matcher = compileP1.matcher(s4);
        if (matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOne"), "EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOneFilter"), "EntityFilterOp:");
            Assert.assertEquals(matcher.group("relation"), "RelationOp");
            Assert.assertEquals(matcher.group("optionalRelFilter"), "RelationFilterOp:");
            Assert.assertEquals(matcher.group("entityTwo"), "EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityTwoFilter"), "EntityFilterOp");
        }


    }

    @Test
    public void calculateStepPattern() throws Exception {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        PlanMockUtils.PlanMockBuilder builder = PlanMockUtils.PlanMockBuilder.mock(asgQuery).entity(TYPED, 100, "4")
                .entityFilter(0.2,7,"6", Constraint.of(ConstraintOp.eq, "equals")).startNewPlan()
                .rel(out, "1", 100).relFilter(0.6,11,"11",Constraint.of(ConstraintOp.ge, "gt")).entity(CONCRETE, 1, "5").entityFilter(1,12,"9", Constraint.of(ConstraintOp.inSet, "inSet"));

        StatisticsProvider provider = build(builder.statistics(), Integer.MAX_VALUE);
        RegexPatternCostEstimator estimator = new RegexPatternCostEstimator(new M1PatternCostEstimator(
                new CostEstimationConfig(1, 0.001),
                (ont) -> provider,
                new OntologyProvider() {
                    @Override
                    public Ontology add(Ontology ontology) {
                        return ontology;
                    }
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }
                }));

        Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost = Optional.of(builder.oldPlanWithCost(50, 250));
        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(builder.plan(), new IncrementalEstimationContext<>(previousCost, asgQuery));

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(), 6);

        Assert.assertTrue(newArrayList(estimate.getCost().getPlanStepCosts()).get(0).getPlan().getOps().get(0) instanceof EntityOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getPlanStepCosts()).get(0).getPlan().getOps().size(), 1);
        Assert.assertTrue(newArrayList(estimate.getCost().getPlanStepCosts()).get(1).getPlan().getOps().get(0) instanceof EntityFilterOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getPlanStepCosts()).get(1).getPlan().getOps().size(), 1);

        Assert.assertEquals(newArrayList(estimate.getCost().getPlanStepCosts()).get(2).getPlan().getOps().size(), 2);
        Assert.assertTrue(newArrayList(estimate.getCost().getPlanStepCosts()).get(2).getPlan().getOps().get(0) instanceof RelationOp);

        Assert.assertEquals(newArrayList(estimate.getCost().getPlanStepCosts()).get(3).getPlan().getOps().size(), 2);
        Assert.assertTrue(newArrayList(estimate.getCost().getPlanStepCosts()).get(3).getPlan().getOps().get(0) instanceof EntityOp);

        Assert.assertEquals(estimate.getCost().getGlobalCost().cost,new DoubleCost(51.06).cost, 0.1);

        Assert.assertEquals(250, newArrayList(estimate.getCost().getPlanStepCosts()).get(0).getCost().peek(), 0);

    }

    @Test
    public void estimateEntityOnlyPattern() throws Exception {
        AsgQuery query = AsgQuery.Builder.start("name", "ont").
                next(concrete(1, "id", "4", "name", "A").next(ePropGroup(101,EProp.of(9, "12", Constraint.of(gt, MALE))))).build();
        PlanMockUtils.PlanMockBuilder builder = PlanMockUtils.PlanMockBuilder.mock(query).entity(TYPED, 100, "4")
                .entityFilter(0.2,7,"6", Constraint.of(ConstraintOp.eq, "equals")).startNewPlan()
                .rel(out, "1", 100).relFilter(0.6,11,"11",Constraint.of(ConstraintOp.ge, "gt")).entity(CONCRETE, 1, "5").entityFilter(1,12,"9", Constraint.of(ConstraintOp.inSet, "inSet"));

        StatisticsProvider provider = build(builder.statistics(), Integer.MAX_VALUE);
        RegexPatternCostEstimator estimator = new RegexPatternCostEstimator(new M1PatternCostEstimator(
                new CostEstimationConfig(1, 0.001),
                (ont) -> provider,
                new OntologyProvider() {
                    @Override
                    public Ontology add(Ontology ontology) {
                        return ontology;
                    }
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }
                }));

        Plan plan = new Plan().withOp(new EntityOp(AsgQueryUtil.element$(query, 1))).withOp(new EntityFilterOp(AsgQueryUtil.element$(query, 101)));
        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(plan, new IncrementalEstimationContext<>(Optional.empty(), query));

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(), 2);
        Assert.assertEquals(estimate.getCost().getGlobalCost().cost, 1, 0);
        Assert.assertEquals(newArrayList(estimate.getCost().getPlanStepCosts()).size(), 1);
        Assert.assertEquals(newArrayList(estimate.getCost().getPlanStepCosts()).get(0).getPlan().getOps().size(), 2);
    }

}
