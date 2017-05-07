package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

import java.util.List;

/**
 * Created by liorp on 4/27/2017.
 */
public interface GraphStatisticsProvider {
    Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema);
    Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices);
    Statistics.Cardinality getEdgeCardinality(GraphEdgeSchema graphEdgeSchema);
    Statistics.Cardinality getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices);

    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphVertexSchema graphVertexSchema,
                                                                                      List<String> relevantIndices,
                                                                                      GraphElementPropertySchema graphElementPropertySchema,
                                                                                      ConstraintOp constraintOp, T value);
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphEdgeSchema graphEdgeSchema,
                                                                                      List<String> relevantIndices,
                                                                                      GraphElementPropertySchema graphElementPropertySchema,
                                                                                      ConstraintOp constraintOp, T value);
}
