package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Elad on 4/26/2017.
 */
public class ConstraintSearchAppender implements SearchAppender<ElementControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        Optional<TraversalConstraint> constraint = context.getConstraint();
        if(!constraint.isPresent()) {
            return true;
        }

        Traversal clone = constraint.get().getTraversal().asAdmin().clone();
        HasStep<?> labelsStep = new TraversalHasStepFinder(hasStep -> !hasStep.getHasContainers().isEmpty() &&
                hasStep.getHasContainers().get(0).getKey().equals(T.label.getAccessor())).getValue(clone);

        List<String> types = Collections.emptyList();
        if (labelsStep != null) {
            List<String> labels = CollectionUtil.listFromObjectValue(Stream.ofAll(labelsStep.getHasContainers()).get(0).getValue());
            if (context.getElementType().equals(ElementType.vertex)) {
                types = Stream.ofAll(labels).filter(label -> context.getSchemaProvider().getVertexSchema(label).isPresent())
                        .map(label -> context.getSchemaProvider().getVertexSchema(label).get().getType())
                        .distinct()
                        .toJavaList();
            } else {
                types = Stream.ofAll(labels).filter(label -> context.getSchemaProvider().getEdgeSchema(label).isPresent())
                        .map(label -> context.getSchemaProvider().getEdgeSchema(label).get().getType())
                        .distinct()
                        .toJavaList();
            }
        }

        if (!types.isEmpty()) {
            HasStep<?> newLabelsStep = new HasStep<>(clone.asAdmin(),
                    new HasContainer(T.label.getAccessor(), types.size() == 1 ? P.eq(types.get(0)) : P.within(types)));
            TraversalHelper.insertAfterStep((Step)newLabelsStep, labelsStep, labelsStep.getTraversal());
            labelsStep.getTraversal().removeStep(labelsStep);
        }

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().filtered().filter().bool().must();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);
        traversalQueryTranslator.visit(clone);
        return true;
    }
}
