package com.kayhut.fuse.epb;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.DummyPlanCostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.SiblingOnlyPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CostCalculator;
import com.kayhut.fuse.model.execution.plan.costs.CostCalculator.Cost;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class EpbModule implements Jooby.Module  {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(SimpleEpbDriver.class).asEagerSingleton();
        binder.bind(new TypeLiteral<PlanSearcher<Plan<CostCalculator.Cost>, AsgQuery>>(){}).to(new TypeLiteral<BottomUpPlanBuilderImpl<Plan<CostCalculator.Cost>, AsgQuery>>(){}).asEagerSingleton();
        DummyCostEstimator planOpCostEstimator = new DummyCostEstimator();
        DummyPlanCostEstimator planCostEstimator = new DummyPlanCostEstimator();
        binder.bind(new TypeLiteral<PlanExtensionStrategy<Plan<CostCalculator.Cost>, AsgQuery>>(){}).toInstance(new CompositePlanExtensionStrategy(new InitialPlanGeneratorExtensionStrategy(planOpCostEstimator, planCostEstimator), new AllDirectionsPlanExtensionStrategy(planOpCostEstimator, planCostEstimator)));
        binder.bind(new TypeLiteral<PlanPruneStrategy<Plan<CostCalculator.Cost>>>(){}).annotatedWith(Names.named("GlobalPruningStrategy")).toInstance(new NoPruningPruneStrategy<>());
        binder.bind(new TypeLiteral<PlanPruneStrategy<Plan<CostCalculator.Cost>>>(){}).annotatedWith(Names.named("LocalPruningStrategy")).toInstance(new NoPruningPruneStrategy<>());
        binder.bind(new TypeLiteral<PlanValidator<Plan<CostCalculator.Cost>, AsgQuery>>(){}).toInstance(new SiblingOnlyPlanValidator<>());

    }
}
