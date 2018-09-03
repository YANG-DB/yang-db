package com.kayhut.fuse.assembly.knowledge;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.epb.plan.statistics.NoStatsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

public class KnowledgeEpbModule  extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {})
                .toProvider(KnowledgePlanSearcherProvider.class);

        binder.bind(StatisticsProviderFactory.class).to(NoStatsProvider.class);
    }
}
