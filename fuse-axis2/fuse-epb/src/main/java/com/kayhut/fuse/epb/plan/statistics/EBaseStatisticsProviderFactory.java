package com.kayhut.fuse.epb.plan.statistics;

import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by Roman on 25/05/2017.
 */
public class EBaseStatisticsProviderFactory implements StatisticsProviderFactory {
    //region Constructor
    @Inject
    public EBaseStatisticsProviderFactory(
            GraphElementSchemaProviderFactory graphElementSchemaProviderFactory,
            GraphStatisticsProvider graphStatisticsProvider) {
        this.graphElementSchemaProviderFactory = graphElementSchemaProviderFactory;
        this.graphStatisticsProvider = graphStatisticsProvider;
    }
    //endregion

    //region StatisticsProviderFactory Implementation
    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new EBaseStatisticsProvider(
                this.graphElementSchemaProviderFactory.get(ontology),
                new Ontology.Accessor(ontology),
                graphStatisticsProvider);
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory graphElementSchemaProviderFactory;
    private GraphStatisticsProvider graphStatisticsProvider;
    //endregion
}
