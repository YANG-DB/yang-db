package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

/**
 * Created by liorp on 4/27/2017.
 */
public interface GraphStatisticsProvider {
    Statistics.HistogramStatistics<String> getVertexCardinality(GraphVertexSchema graphVertexSchema);
}
