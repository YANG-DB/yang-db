package com.yangdb.fuse.epb.plan.statistics;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.google.inject.Provider;
import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.yangdb.fuse.epb.plan.statistics.configuration.ElasticCountStatisticsConfig;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.model.ontology.Ontology;
import com.typesafe.config.Config;

import javax.inject.Inject;

public class ElasticCountStatisticsProviderFactory implements StatisticsProviderFactory {
    private PlanTraversalTranslator planTraversalTranslator;
    private Provider<UniGraphProvider> uniGraphProvider;
    private ElasticCountStatisticsConfig elasticCountStatisticsConfig;

    @Inject
    public ElasticCountStatisticsProviderFactory(PlanTraversalTranslator planTraversalTranslator, Provider<UniGraphProvider> uniGraphProvider, Config config) {
        this.planTraversalTranslator = planTraversalTranslator;
        this.uniGraphProvider = uniGraphProvider;
        elasticCountStatisticsConfig = new ElasticCountStatisticsConfig(config);
    }

    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new ElasticCountStatisticsProvider(planTraversalTranslator, ontology, uniGraphProvider, elasticCountStatisticsConfig);
    }
}
