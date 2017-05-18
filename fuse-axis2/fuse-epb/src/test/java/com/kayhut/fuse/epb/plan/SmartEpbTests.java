package com.kayhut.fuse.epb.plan;

import com.google.common.collect.Iterables;
import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.BasicStepEstimator;
import com.kayhut.fuse.epb.plan.extenders.CompoundStepExtenderStrategy;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.epb.tests.BasicScenarioSetup;
import com.kayhut.fuse.epb.tests.ScenarioMockUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by moti on 5/18/2017.
 */
public class SmartEpbTests {

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;

    @Before
    public void setup(){
        ScenarioMockUtil scenarioMockUtil = BasicScenarioSetup.setup();
        EBaseStatisticsProvider eBaseStatisticsProvider = new EBaseStatisticsProvider(scenarioMockUtil.getGraphElementSchemaProvider(), scenarioMockUtil.getOntology(), scenarioMockUtil.getGraphStatisticsProvider());
        StatisticsCostEstimator statisticsCostEstimator = new StatisticsCostEstimator(eBaseStatisticsProvider, scenarioMockUtil.getGraphElementSchemaProvider(), scenarioMockUtil.getOntology(), new BasicStepEstimator(1.0,0.001 ));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        planSearcher = new BottomUpPlanSearcher<>(
                new CompoundStepExtenderStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                statisticsCostEstimator);

    }

    @Test
    public void testSingleElement(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed( 1,"D", 1)).
                next(eProp(2,EProp.of("1", 2, Constraint.of(ConstraintOp.eq, "abc")))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(first.getCost().getGlobalCost(),new Cost(10));
        Assert.assertEquals(first.getCost().getOpCosts().iterator().next().getCost(),new Cost(10));
    }
/*
    @Test
    public void testFullStep(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, "A", 1))
                .next(eProp(12))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("8", 10, of(eq, new Date())))))
                .next(typed(2, "B", 3))
                .next(eProp(14))
                .build();

        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(first.getCost().getGlobalCost(),new Cost(10));
    }
    */
}
