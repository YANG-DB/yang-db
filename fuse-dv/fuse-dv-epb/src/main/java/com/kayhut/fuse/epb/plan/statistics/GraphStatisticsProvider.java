package com.kayhut.fuse.epb.plan.statistics;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
 * #L%
 */

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

import java.util.List;

/**
 * Created by lior.perry on 4/27/2017.
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
