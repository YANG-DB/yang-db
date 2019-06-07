package com.kayhut.fuse.unipop.controller.discrete.converter;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.search.SearchHit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 11/15/2017.
 */
public class DiscreteVertexFilterConverter implements ElementConverter<SearchHit, Edge> {
    //region Constructor
    public DiscreteVertexFilterConverter(VertexControllerContext context) {
        this.context = context;
        this.typeToLabelVertexSchemas = Stream.ofAll(context.getSchemaProvider().getVertexSchemas())
                .toJavaMap(vertexSchema ->
                        new Tuple2<>(
                                Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(
                                        vertexSchema.getConstraint().getTraversalConstraint(),
                                        T.label.getAccessor()))
                                        .get(0),
                                vertexSchema));
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<Edge> convert(SearchHit hit) {
        Vertex contextVertex = context.getVertex(hit.getId());
        Map<String, Object> contextVertexProperties =
                Stream.ofAll(contextVertex::properties).toJavaMap(property -> new Tuple2<>(property.key(), property.value()));

        //        ##Es 5 optimization for searchHit deprecated Logger
        //        Map<String, Object> source = SearchHitUtils.convertToMap(hit);
        Map<String, Object> source = new HashMap<>(hit.getSourceAsMap());
        contextVertexProperties.putAll(source);

        DiscreteVertex v = new DiscreteVertex(
                hit.getId(),
                this.typeToLabelVertexSchemas.get(source.get("type")).getLabel(),
                context.getGraph(),
                contextVertexProperties);

        return Collections.singletonList(new DiscreteEdge(
                v.id(),
                GlobalConstants.Labels.PROMISE_FILTER,
                v,
                v,
                v,
                context.getGraph(),
                Collections.emptyMap()));
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    private Map<String, GraphVertexSchema> typeToLabelVertexSchemas;
    //endregion
}
