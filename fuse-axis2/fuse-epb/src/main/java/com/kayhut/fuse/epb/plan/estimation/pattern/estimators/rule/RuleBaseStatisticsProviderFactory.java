package com.kayhut.fuse.epb.plan.estimation.pattern.estimators.rule;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.*;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;


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
