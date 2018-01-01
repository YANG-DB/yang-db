package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import com.kayhut.fuse.unipop.structure.promise.PromiseFilterEdge;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 11/15/2017.
 */
public class DiscreteVertexFilterConverter implements ElementConverter<SearchHit, Edge> {
    //region Constructor
    public DiscreteVertexFilterConverter(ElementControllerContext context) {
        this.context = context;
        this.typeToLabelVertexSchemas = Stream.ofAll(context.getSchemaProvider().getVertexLabels())
                .map(label -> context.getSchemaProvider().getVertexSchema(label))
                .filter(Optional::isPresent)
                .map(Optional::get)
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
        DiscreteVertex v = new DiscreteVertex(
                hit.getId(),
                this.typeToLabelVertexSchemas.get((String)hit.getSourceAsMap().get("type")).getLabel(),
                context.getGraph(),
                hit.getSourceAsMap());

        return Collections.singletonList(new DiscreteEdge(
                v.id(),
                GlobalConstants.Labels.PROMISE_FILTER,
                v,
                v,
                context.getGraph(),
                Collections.emptyMap()));
    }
    //endregion

    //region Fields
    private ElementControllerContext context;
    private Map<String, GraphVertexSchema> typeToLabelVertexSchemas;
    //endregion
}
