package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    }
    //endregion
}
