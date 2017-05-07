package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;

import java.util.List;

/**
 * Created by liorp on 4/27/2017.
 */
public interface GraphStatisticsProvider {
    Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema);
    Statistics.Cardinality getVertexCardinality(GraphVertexSchema graphVertexSchema, List<IndexPartition> relevantPartitions);

    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphVertexSchema graphVertexSchema,
                                                                                      List<IndexPartition> relevantPartitions,
                                                                                      GraphElementPropertySchema graphElementPropertySchema,
                                                                                      ConstraintOp constraintOp, T value);

}
