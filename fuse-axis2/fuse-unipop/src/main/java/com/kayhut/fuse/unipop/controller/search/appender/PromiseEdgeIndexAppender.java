package com.kayhut.fuse.unipop.controller.search.appender;

import com.google.common.collect.Lists;
import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.AndStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeIndexAppender implements SearchAppender<PromiseVertexControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext context) {

        Optional<String> edgeLabel = getEdgeLabel(context.getEdgeConstraint());

        if(edgeLabel.isPresent()) {
            Optional<GraphEdgeSchema> edgeSchema = context.getSchema().getEdgeSchema(edgeLabel.get(), Optional.empty(), Optional.empty());
            if(edgeSchema.isPresent()) {
                searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchema.get().getIndexPartitions()));
            }
        }
        return true;

    }

    private Optional<String> getEdgeLabel(Optional<TraversalConstraint> edgeConstraint) {

        if (edgeConstraint.isPresent()) {
            List<Step> steps = edgeConstraint.get().getTraversal().asAdmin().getSteps();
            for (Step step : steps) {
                Optional<String> label = findEdgeLabelInStep(step);
                if(label.isPresent()) {
                    return label;
                }
            }
        }

        return Optional.empty();
    }

    private Optional<String> findEdgeLabelInStep(Step step) {

        if (step instanceof HasStep) {
            List<HasContainer> hasContainers = ((HasStep) step).getHasContainers();
            for (HasContainer hasContainer :
                    hasContainers
                    ) {
                if (hasContainer.getKey().equals(T.label)) {
                    return Optional.of((String) hasContainer.getValue());
                }
            }
        }

        //TODO: recursively visit all child steps...

        return Optional.empty();
    }

    private List<String> getEdgeSchemasIndices(Iterable<IndexPartition> indexPartitions) {
        List<String> indices = new ArrayList<>();
        indexPartitions.forEach(indexPartition -> indices.addAll(Lists.newArrayList(indexPartition.getIndices())));
        return indices;
    }
}
