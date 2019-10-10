package com.yangdb.fuse.epb.plan.estimation.pattern.estimators.rule;

/*-
 *
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.google.inject.Inject;
import com.yangdb.fuse.epb.plan.statistics.*;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.ontology.Ontology;


public class RuleBaseStatisticsProviderFactory implements StatisticsProviderFactory {
    //region Constructor
    @Inject
    public RuleBaseStatisticsProviderFactory(GraphElementSchemaProviderFactory graphElementSchemaProviderFactory,
                                             RuleBasedStatisticalProvider statisticalProvider) {
        this.graphElementSchemaProviderFactory = graphElementSchemaProviderFactory;
        this.statisticalProvider = statisticalProvider;
    }
    //endregion

    //region StatisticsProviderFactory Implementation
    @Override
    public StatisticsProvider get(Ontology ontology) {
        if(this.graphElementSchemaProviderFactory.get(ontology)!=null)
            return statisticalProvider;
        //not the correct ontology
        throw new IllegalArgumentException("Ontology "+ontology.getOnt()+" has no RuleBasedStatisticalProvider implementation");
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory graphElementSchemaProviderFactory;
    private RuleBasedStatisticalProvider statisticalProvider;
    //endregion
}
