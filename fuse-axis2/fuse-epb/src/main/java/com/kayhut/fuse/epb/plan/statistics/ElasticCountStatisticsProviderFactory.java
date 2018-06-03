package com.kayhut.fuse.epb.plan.statistics;

import com.google.inject.Provider;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.epb.plan.statistics.configuration.ElasticCountStatisticsConfig;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.ontology.Ontology;
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
