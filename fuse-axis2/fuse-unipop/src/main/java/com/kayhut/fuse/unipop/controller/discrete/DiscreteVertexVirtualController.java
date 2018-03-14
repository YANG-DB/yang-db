package com.kayhut.fuse.unipop.controller.discrete;

import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.unipop.query.search.SearchVertexQuery;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by roman.margolis on 14/03/2018.
 */
public class DiscreteVertexVirtualController extends VertexControllerBase {
    //region Constructors
    public DiscreteVertexVirtualController(SearchVertexQuery.SearchVertexController innerController, GraphElementSchemaProvider schemaProvider) {
        super(labels -> Stream.ofAll(labels).isEmpty() ||
                        Stream.ofAll(schemaProvider.getEdgeLabels()).toJavaSet().containsAll(Stream.ofAll(labels).toJavaSet()),
                Stream.ofAll(schemaProvider.getEdgeLabels()).toJavaSet());

        this.schemaProvider = schemaProvider;
        this.innerController = innerController;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey()
                        .toLowerCase()
                        .equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();

        if (constraintHasContainers.size() > 1) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if (constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        if (Stream.ofAll(edgeLabels).size() == 1) {
            String edgeLabel = Stream.ofAll(edgeLabels).get(0);

            //currently assuming same vertex in bulk
            String vertexLabel = searchVertexQuery.getVertices().get(0).label();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabel, searchVertexQuery.getDirection(), edgeLabel);

            //currently assuming a single relevant edge schema
            GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

            if (Stream.ofAll(edgeSchema.getEndB().get().getIdFields()).size() == 1) {
                String idField = Stream.ofAll(edgeSchema.getEndB().get().getIdFields()).get(0);

                if (idField == "_id") {

                } else {
                    VertexProperty<String> idProperty = searchVertexQuery.getVertices().get(0).property(idField);
                    if (idProperty.equals(VertexProperty.empty())) {
                        return this.innerController.search(searchVertexQuery);
                    }

                    return Stream.ofAll(searchVertexQuery.getVertices())
                            .map(vertex -> new DiscreteEdge())
                }
            }
        }
    }
    //endregion

    //region Fields
    private SearchVertexQuery.SearchVertexController innerController;
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}
