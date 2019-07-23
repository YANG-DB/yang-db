package com.yangdb.fuse.unipop.controller.common.appender;

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

import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.common.context.ElementControllerContext;
import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.yangdb.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.AndStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Elad on 4/26/2017.
 */
public class ConstraintSearchAppender implements SearchAppender<CompositeControllerContext> {
    //region ElementControllerContext Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, CompositeControllerContext context) {
        Set<String> labels = getContextRelevantLabels(context);

        Traversal newConstraint = context.getConstraint().isPresent() ?
                context.getConstraint().get().getTraversal().asAdmin().clone() :
                __.start().has(T.label, P.within(labels));

        List<GraphElementConstraint> elementConstraints =
                    context.getElementType().equals(ElementType.vertex) ?
                            Stream.ofAll(labels)
                                    .flatMap(label -> context.getSchemaProvider().getVertexSchemas(label))
                                    .map(p->p.getConstraint())
                                    .toJavaList() :
                            Stream.ofAll(context.getSchemaProvider().getEdgeSchemas(
                                    Stream.ofAll(context.getBulkVertices()).get(0).label(),
                                    context.getDirection(),
                                    Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor())).get(0)))
                                    .map(p->p.getConstraint())
                                    .toJavaList();

        if (!elementConstraints.isEmpty()) {
            List<HasStep> labelHasSteps = Stream.ofAll(new TraversalHasStepFinder(step -> step.getHasContainers().get(0).getKey().equals(T.label.getAccessor()))
                    .getValue(newConstraint)).toJavaList();

            if (!labelHasSteps.isEmpty()) {
                labelHasSteps.get(0).getTraversal().removeStep(labelHasSteps.get(0));
            }

            Traversal elementConstraintsTraversal = elementConstraints.size() > 1 ?
                    __.start().or(Stream.ofAll(elementConstraints).map(GraphElementConstraint::getTraversalConstraint).toJavaArray(Traversal.class)) :
                    elementConstraints.get(0).getTraversalConstraint();

            newConstraint = Stream.ofAll(new TraversalHasStepFinder(step -> true).getValue(newConstraint)).isEmpty() ?
                    elementConstraintsTraversal :
                    __.start().and(elementConstraintsTraversal, newConstraint);

        }

        if (!(newConstraint.asAdmin().getSteps().get(0) instanceof AndStep)) {
            newConstraint = __.start().and(newConstraint);
        }

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query();//.filtered().filter();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);
        traversalQueryTranslator.visit(newConstraint);
        return true;
    }
    //endregion

    //region Private Methods
    private Set<String> getContextRelevantLabels(CompositeControllerContext context) {
        if (context.getVertexControllerContext().isPresent()) {
            return getVertexContextRelevantLabels(context);
        }

        return getElementContextRelevantLabels(context);
    }

    private Set<String> getElementContextRelevantLabels(ElementControllerContext context) {
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

        return labels;
    }

    private Set<String> getVertexContextRelevantLabels(VertexControllerContext context) {
        // currently assuming homogeneous bulk
        return Stream.ofAll(context.getBulkVertices())
                .take(1)
                .map(Element::label)
                .toJavaSet();
    }
    //endregion
}
