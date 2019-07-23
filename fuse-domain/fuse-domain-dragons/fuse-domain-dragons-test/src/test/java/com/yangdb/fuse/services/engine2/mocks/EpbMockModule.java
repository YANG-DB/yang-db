package com.yangdb.fuse.services.engine2.mocks;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.yangdb.fuse.dispatcher.epb.PlanSearcher;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.epb.plan.statistics.NoStatsProvider;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

public class EpbMockModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){}).toInstance(query -> plan);
                this.bind(StatisticsProviderFactory.class).to(NoStatsProvider.class);
                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){});
            }
        });
    }


    public static PlanWithCost<Plan, PlanDetailedCost> plan;
}
