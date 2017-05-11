package com.kayhut.fuse.unipop.controller.search.appender;

import com.google.common.collect.Lists;
import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.PromiseEdgeConstants;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

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
            Optional<GraphEdgeSchema> edgeSchema = context.getSchema().getEdgeSchema(edgeLabel.get());
            if(edgeSchema.isPresent()) {
                searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchema.get().getIndexPartition()));
            }
        }
        return true;

    }

    private Optional<String> getEdgeLabel(Optional<TraversalConstraint> edgeConstraint) {

        if (!edgeConstraint.isPresent()) {
            return Optional.empty();
        }

        List<HasStep> hasSteps = TraversalHelper.getStepsOfAssignableClassRecursively(HasStep.class, edgeConstraint.get().getTraversal().asAdmin());

        for (HasStep step : hasSteps) {
            Optional<String> label = findEdgeLabelInStep(step);
            if (label.isPresent()) {
                return label;
            }
        }

        return Optional.empty();
    }

    private Optional<String> findEdgeLabelInStep(HasStep step) {

        List<HasContainer> hasContainers = step.getHasContainers();
        for (HasContainer hasContainer :
                hasContainers
                ) {
            if (hasContainer.getKey().equals(PromiseEdgeConstants.EDGE_LABEL_CONSTRAINT_KEY)) {
                return Optional.of((String) hasContainer.getPredicate().getValue());
            }
        }
        return Optional.empty();
    }

    private List<String> getEdgeSchemasIndices(IndexPartition indexPartition) {
        return Lists.newArrayList(indexPartition.getIndices());
    }
}
