package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

import java.util.List;

/**
 * Created by liorp on 4/27/2017.
 */
public interface GraphStatisticsProvider {
    Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema);
    Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices);
    Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema);
    Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices);

    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphElementSchema,
                                                                                      List<String> relevantIndices,
                                                                                      GraphElementPropertySchema graphElementPropertySchema,
                                                                                      Constraint constraint, Class<T> javaType);

    long getGlobalSelectivity(GraphEdgeSchema graphEdgeSchema, Rel.Direction direction, List<String> relevantIndices);

}
