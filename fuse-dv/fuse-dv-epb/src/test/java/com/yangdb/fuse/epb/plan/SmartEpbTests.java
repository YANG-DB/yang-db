package com.yangdb.fuse.epb.plan;

import com.yangdb.fuse.dispatcher.epb.PlanPruneStrategy;
import com.yangdb.fuse.dispatcher.epb.PlanSelector;
import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.epb.plan.estimation.CostEstimationConfig;
import com.yangdb.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.yangdb.fuse.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import com.yangdb.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.yangdb.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.yangdb.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.epb.utils.BasicScenarioSetup;
import com.yangdb.fuse.epb.utils.ScenarioMockUtil;
import com.yangdb.fuse.model.OntologyTestUtils.PERSON;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.DoubleCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.model.OntologyTestUtils.FIRST_NAME;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.typed;

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
                new OntologyProvider() {
                    @Override
                    public Ontology add(Ontology ontology) {
                        return ontology;
                    }
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(scenarioMockUtil.getOntologyAccessor().get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(scenarioMockUtil.getOntologyAccessor().get());
                    }
                }));

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
                next(AsgQuery.Builder.ePropGroup(2,EProp.of(2, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc")))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getCost().getGlobalCost(),new DoubleCost(10));
        Assert.assertEquals(new CountEstimatesCost(10, 10), plan.getCost().getPlanStepCosts().iterator().next().getCost());
    }
}
