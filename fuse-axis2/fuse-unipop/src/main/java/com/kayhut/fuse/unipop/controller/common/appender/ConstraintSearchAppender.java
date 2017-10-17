package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
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

        Traversal newConstraint = context.getConstraint().isPresent() ?
                context.getConstraint().get().getTraversal().asAdmin().clone() :
                __.has(T.label, P.within(labels));

        List<GraphElementConstraint> elementConstraints =
                Stream.ofAll(labels)
                        .flatMap(label -> context.getElementType().equals(ElementType.vertex) ?
                                context.getSchemaProvider().getVertexSchema(label).isPresent() ?
                                        Collections.singletonList(context.getSchemaProvider().getVertexSchema(label).get()) :
                                        Collections.emptyList() :
                                new EdgeSchemaSupplier((VertexControllerContext)context).labels().applicable().get())
                .map(GraphElementSchema::getConstraint)
                .toJavaList();

        if (!elementConstraints.isEmpty()) {
            List<HasStep> labelHasSteps = Stream.ofAll(new TraversalHasStepFinder(step -> step.getHasContainers().get(0).getKey().equals(T.label.getAccessor()))
                    .getValue(newConstraint)).toJavaList();

            if (!labelHasSteps.isEmpty()) {
                labelHasSteps.get(0).getTraversal().removeStep(labelHasSteps.get(0));
            }

            newConstraint = __.and(
                    __.or(Stream.ofAll(elementConstraints).map(GraphElementConstraint::getTraversalConstraint).toJavaArray(Traversal.class)),
                    newConstraint);
        }

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().filtered().filter();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);
        traversalQueryTranslator.visit(newConstraint);
        return true;
    }
}
