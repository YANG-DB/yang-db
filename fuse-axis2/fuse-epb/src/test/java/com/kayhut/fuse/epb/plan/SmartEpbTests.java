package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.epb.utils.BasicScenarioSetup;
import com.kayhut.fuse.epb.utils.ScenarioMockUtil;
import com.kayhut.fuse.model.OntologyTestUtils.PERSON;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.FIRST_NAME;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;

/**
 * Created by moti on 5/18/2017.
 */
public class SmartEpbTests {

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;

    @Before
    public void setup(){
        ScenarioMockUtil scenarioMockUtil = BasicScenarioSetup.setup();
        EBaseStatisticsProvider eBaseStatisticsProvider = new EBaseStatisticsProvider(
                scenarioMockUtil.getGraphElementSchemaProvider(),
                scenarioMockUtil.getOntologyAccessor(),
                scenarioMockUtil.getGraphStatisticsProvider());

        RegexPatternCostEstimator estimator = new RegexPatternCostEstimator(new M1PatternCostEstimator(
                new CostEstimationConfig(1.0, 0.001),
                (ont) -> eBaseStatisticsProvider,
                (id) -> Optional.of(scenarioMockUtil.getOntologyAccessor().get())));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        planSearcher = new BottomUpPlanSearcher<>(
                new M1NonRedundantPlanExtensionStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                estimator);
    }

    @Test
    public void testSingleElement(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2,EProp.of(FIRST_NAME.type, 2, Constraint.of(ConstraintOp.eq, "abc")))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getCost().getGlobalCost(),new DoubleCost(10));
        Assert.assertEquals(new CountEstimatesCost(10, 10), plan.getCost().getPlanStepCosts().iterator().next().getCost());
    }
}
