package com.kayhut.fuse.assembly.knowledge;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.kayhut.fuse.assembly.knowledge.cursor.KnowledgeGraphHierarchyTraversalCursor;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.dispatcher.epb.LoggingPlanSearcher;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.epb.plan.BottomUpPlanSearcher;
import com.kayhut.fuse.epb.plan.statistics.NoStatsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class KnowledgeModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        String indexName = conf.getString(conf.getString("assembly") + ".idGenerator_indexName");
        binder.bindConstant().annotatedWith(named(KnowledgeIdGenerator.indexNameParameter)).to(indexName);
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>() {}).to(KnowledgeIdGenerator.class).asEagerSingleton();

        Multibinder<CompositeCursorFactory.Binding> bindingMultibinder = Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class);
        //KnowledgeGraphHierarchyCursor
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                KnowledgeGraphHierarchyCursorRequest.CursorType,
                KnowledgeGraphHierarchyCursorRequest.class,
                new KnowledgeGraphHierarchyTraversalCursor.Factory()));


        binder.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>() {})
                .toProvider(KnowledgePlanSearcherProvider.class);

        binder.bind(StatisticsProviderFactory.class).to(NoStatsProvider.class);
    }
    //endregion
}
