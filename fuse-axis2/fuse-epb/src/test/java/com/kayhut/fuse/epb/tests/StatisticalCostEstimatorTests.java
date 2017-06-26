package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.BasicStepEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.M1StepEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.StepEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import javaslang.Tuple2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.getSupportedPattern;
import static com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder.mock;
import static com.kayhut.fuse.epb.tests.PlanMockUtils.Type.CONCRETE;
import static com.kayhut.fuse.epb.tests.PlanMockUtils.Type.TYPED;
import static com.kayhut.fuse.epb.tests.StatisticsMockUtils.build;
import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.execution.plan.Direction.out;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
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
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(START_DATE.type, 10, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(NAME.type, 9, Constraint.of(eq, "smith")), EProp.of(GENDER.type, 9, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(START_DATE.type, 11,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 11, Constraint.of(le, new Date(time + 1000 * 60)))))
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
                return Optional.of(new GraphRedundantPropertySchema() {
                    @Override
                    public String getName() {
                        return "lastName";
                    }

                    @Override
                    public String getType() {
                        return "string";
                    }

                    @Override
                    public String getPropertyRedundantName() {
                        return "entityB.lastName";
                    }
                });
            }


            return Optional.empty();


        });
        when(graphEdgeSchema.getDestination()).thenReturn(Optional.of(edgeEnd));
        when(graphElementSchemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(graphEdgeSchema));
        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    }

    @Test
    public void planEstimatorPatternTwoTest() {

        StatisticsCostEstimator.StatisticsCostEstimatorPatterns[] supportedPattern = getSupportedPattern();

        String s1 = new Plan().withOp(new EntityOp()).toPattern();

        String s2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).toPattern();

        String s3 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationFilterOp()).toPattern();

        Pattern compileP2 = Pattern.compile(supportedPattern[1].pattern());

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
        StatisticsCostEstimator.StatisticsCostEstimatorPatterns[] supportedPattern = getSupportedPattern();

        String s1 = new Plan().withOp(new EntityOp()).toPattern();

        String s2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).toPattern();

        String s3 = new Plan().withOp(new EntityOp()).withOp(new RelationOp()).withOp(new EntityOp()).toPattern();

        String s4 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationOp()).withOp(new RelationFilterOp()).withOp(new EntityOp()).withOp(new EntityFilterOp()).toPattern();


        Pattern compileP1 = Pattern.compile(supportedPattern[0].pattern());

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
        StatisticsCostEstimator estimator = new StatisticsCostEstimator(
                (ont) -> provider,
                M1StepEstimator.getStepEstimator(1, 0.001),
                (id) -> Optional.of(ont.get()));

        Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost = Optional.of(builder.oldPlanWithCost(50, 250));
        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(builder.plan(), previousCost, asgQuery);

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(), 6);

        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().size(), 1);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(1).getOpBase().get(0) instanceof EntityFilterOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(1).getOpBase().size(), 1);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().size(), 2);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().get(0) instanceof RelationOp);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().size(), 2);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().get(0) instanceof EntityOp);

        Assert.assertEquals(estimate.getCost().getGlobalCost().cost,new Cost(51.06).cost,0.1);

        Assert.assertEquals(250, newArrayList(estimate.getCost().getOpCosts()).get(0).peek(), 0);

    }

    private StepEstimator mockStepEstimator() {
        StepEstimator mock = Mockito.mock(StepEstimator.class);
        when(mock.calculate(any(), any(), any(), any())).thenAnswer(invocationOnMock -> {
            if (!invocationOnMock.getArgumentAt(3, Optional.class).isPresent())
                return new Tuple2<>(1d, Collections.emptyList());
            else
                return new Tuple2<>(1d,
                        ((PlanWithCost<Plan, Cost>) invocationOnMock.getArgumentAt(3, Optional.class).get())
                                .getPlan().getOps().stream().map(p -> new PlanOpWithCost(1, 1, p))
                                .collect(Collectors.toList()));
        });
        return mock;
    }

    @Test
    public void estimateEntityOnlyPattern() throws Exception {
        StatisticsProvider provider = build(Collections.emptyMap(), Integer.MAX_VALUE);
        StatisticsCostEstimator estimator = new StatisticsCostEstimator(
                (ont) -> provider,
                M1StepEstimator.getStepEstimator(1, 0.001),
                (id) -> Optional.of(ont.get()));

        AsgQuery query = AsgQuery.Builder.start("name", "ont").
                next(concrete(1, "id", "1", "name", "A")).
                build();

        EntityOp entityOp = new EntityOp(AsgQueryUtil.element$(query, 1));

        Plan plan = new Plan().withOp(entityOp);
        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(plan, Optional.empty(), query);

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(), 2);
        Assert.assertEquals(estimate.getCost().getGlobalCost().cost, 1, 0);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).size(), 1);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().size(), 2);
    }

}
