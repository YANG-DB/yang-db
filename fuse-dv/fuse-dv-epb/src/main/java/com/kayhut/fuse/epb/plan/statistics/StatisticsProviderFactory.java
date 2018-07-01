package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by Roman on 25/05/2017.
 */
public interface StatisticsProviderFactory {
    StatisticsProvider get(Ontology ontology);
}
