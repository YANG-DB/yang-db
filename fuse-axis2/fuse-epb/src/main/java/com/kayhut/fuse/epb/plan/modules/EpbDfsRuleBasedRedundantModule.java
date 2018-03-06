package com.kayhut.fuse.epb.plan.modules;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanTracer;
import com.kayhut.fuse.epb.plan.BottomUpPlanSearcher;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.FirstStepOnlyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

public class EpbDfsRuleBasedRedundantModule extends BaseEpbModule {
    //region Private Methods
    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy() {
        return M1DfsRedundantPlanExtensionStrategy.class;
    }

    @Override
    protected void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(CostEstimationConfig.class)
                            .toInstance(new CostEstimationConfig(conf.getDouble("epb.cost.alpha"), conf.getDouble("epb.cost.delta")));
                    this.bind(new TypeLiteral<PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .to(M1PatternCostEstimator.class).asEagerSingleton();
                    this.bind(StatisticsProviderFactory.class).to(getStatisticsProviderFactory(conf)).asEagerSingleton();
                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .annotatedWith(named(RegexPatternCostEstimator.costEstimatorName))
                            .to(RegexPatternCostEstimator.class).asEagerSingleton();
                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                            .to(FirstStepOnlyCostEstimator.class).asEagerSingleton();
                    this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(FirstStepOnlyCostEstimator.class.getSimpleName());
                    this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                            .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});

                    this.expose(StatisticsProviderFactory.class);
                    this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected PlanPruneStrategy<PlanWithCost<Plan,PlanDetailedCost>> globalPrunerStrategy() {
        return new CheapestPlanPruneStrategy();
    }


    protected Class<? extends StatisticsProviderFactory> getStatisticsProviderFactory(Config conf) throws ClassNotFoundException {
        return (Class<? extends StatisticsProviderFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".stats_provider_class"));
    }

//endregion
}
