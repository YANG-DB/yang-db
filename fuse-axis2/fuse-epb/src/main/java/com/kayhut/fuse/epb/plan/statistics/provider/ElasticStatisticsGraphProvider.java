package com.kayhut.fuse.epb.plan.statistics.provider;

import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

import java.util.List;

/**
 * Created by benishue on 22-May-17.
 */
public class ElasticStatisticsGraphProvider implements GraphStatisticsProvider {
    @Override
    public Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema) {
        return null;
    }

    @Override
    public Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices) {
        return null;
    }

    @Override
    public Statistics.Cardinality getEdgeCardinality(GraphEdgeSchema graphEdgeSchema) {
        return null;
    }

    @Override
    public Statistics.Cardinality getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphVertexSchema, List<String> relevantIndices, GraphElementPropertySchema graphElementPropertySchema, Constraint constraint, T value) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphEdgeSchema, List<String> relevantIndices, GraphElementPropertySchema graphElementPropertySchema, Constraint constraint, List<T> values) {
        return null;
    }

    @Override
    public long getGlobalSelectivity(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices) {
        return 0;
    }
}
