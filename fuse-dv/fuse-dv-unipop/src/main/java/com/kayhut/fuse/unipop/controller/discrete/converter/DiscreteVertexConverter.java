package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.utils.elasticsearch.SearchHitUtils;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
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
    public Iterable<E> convert(SearchHit searchHit) {
        //Map<String, Object> source = new HashMap<>(searchHit.sourceAsMap());
        Map<String, Object> source = SearchHitUtils.convertToMap(searchHit);

        if(searchHit.getScore() > 0){
            source.put("score", searchHit.getScore());
        }
        return Arrays.asList((E)new DiscreteVertex(
                searchHit.getId(),
                this.typeToLabelVertexSchemas.get(source.get("type")).getLabel(),
                context.getGraph(),
                source));
    }
    //endregion

    //region Fields
    private ElementControllerContext context;
    private Map<String, GraphVertexSchema> typeToLabelVertexSchemas;
    //endregion
}
