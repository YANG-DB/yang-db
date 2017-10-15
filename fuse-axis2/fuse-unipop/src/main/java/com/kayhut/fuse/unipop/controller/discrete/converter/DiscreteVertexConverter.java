package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.elasticsearch.search.SearchHit;

import java.util.*;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteVertexConverter<E extends Element> implements ElementConverter<SearchHit, E> {
    //region Constructors
    public DiscreteVertexConverter(ElementControllerContext context) {
        this.context = context;

        Set<String> labels = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (labels.isEmpty()) {
            labels = Stream.ofAll(context.getElementType().equals(ElementType.vertex) ?
                    context.getSchemaProvider().getVertexLabels() :
                    context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        this.vertexSchemas = Stream.ofAll(labels)
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
    public E convert(SearchHit searchHit) {
        return (E)new DiscreteVertex(
                searchHit.getId(),
                this.vertexSchemas.get(searchHit.getType()).getLabel(),
                context.getGraph(),
                searchHit.sourceAsMap());
    }
    //endregion

    //region Fields
    private ElementControllerContext context;
    private Map<String, GraphVertexSchema> vertexSchemas;
    //endregion
}
