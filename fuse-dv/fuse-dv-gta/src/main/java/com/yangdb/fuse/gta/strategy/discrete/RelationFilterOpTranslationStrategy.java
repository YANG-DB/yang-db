package com.yangdb.fuse.gta.strategy.discrete;

/*-
 * #%L
 * fuse-dv-gta
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.yangdb.fuse.gta.strategy.utils.ConversionUtil;
import com.yangdb.fuse.gta.strategy.utils.TraversalUtil;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.properties.constraint.WhereByConstraint;
import com.yangdb.fuse.unipop.promise.Constraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.GlobalConstants.HasKeys.CONSTRAINT;

/**
 * Created by Roman on 09/05/2017.
 */
public class RelationFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationFilterOpTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(RelationFilterOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        RelationFilterOp relationFilterOp = (RelationFilterOp)planOp;
        Optional<RelationOp> relationOp = PlanUtil.adjacentPrev(plan.getPlan(), relationFilterOp);
        if (!relationOp.isPresent()) {
            return traversal;
        }

        TraversalUtil.remove(traversal, TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class));

        traversal = appendRelationAndPropertyGroup(
                traversal,
                relationOp.get().getAsgEbase().geteBase(),
                relationFilterOp.getAsgEbase().geteBase(),
                context.getOnt());

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendRelationAndPropertyGroup(
            GraphTraversal traversal,
            Rel rel,
            RelPropGroup relPropGroup,
            Ontology.Accessor ont) {

        String relationTypeName = ont.$relation$(rel.getrType()).getName();

        List<Traversal> relPropGroupTraversals = Collections.emptyList();
        if (!relPropGroup.getProps().isEmpty() || !relPropGroup.getGroups().isEmpty()) {
            relPropGroupTraversals = Collections.singletonList(convertRelPropGroupToTraversal(relPropGroup, ont));
        }


        List<Traversal> traversals = Stream.<Traversal>of(__.start().has(T.label, P.eq(relationTypeName)))
                .appendAll(relPropGroupTraversals).toJavaList();

        return traversals.size() == 1 ?
                traversal.has(CONSTRAINT, Constraint.by(traversals.get(0))) :
                traversal.has(CONSTRAINT, Constraint.by(__.start().and(Stream.ofAll(traversals).toJavaArray(Traversal.class))));
    }
    //endregion

    //region Private Methods
    private Traversal convertRelPropGroupToTraversal(RelPropGroup relPropGroup, Ontology.Accessor ont) {
        List<Traversal> childGroupTraversals = Stream.ofAll(relPropGroup.getGroups())
                .map(childGroup -> convertRelPropGroupToTraversal(childGroup, ont))
                .toJavaList();

        List<Traversal> epropTraversals = Stream.ofAll(relPropGroup.getProps())
                .filter(relProp -> relProp.getCon() != null)
                .filter(relProp -> !(relProp.getCon() instanceof WhereByConstraint))
                .map(relProp -> convertRelPropToTraversal(relProp, ont))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        Traversal[] traversals = Stream.ofAll(epropTraversals).appendAll(childGroupTraversals).toJavaArray(Traversal.class);

        switch (relPropGroup.getQuantType()) {
            case all:
                if (traversals.length == 1) {
                    return traversals[0];
                }

                return __.start().and(traversals);
            case some: return __.start().or(traversals);

            default: return __.start().and(traversals);
        }
    }

    private Optional<Traversal> convertRelPropToTraversal(RelProp relProp, Ontology.Accessor ont) {
        Optional<Property> property = ont.$property(relProp.getpType());
        if (property.isPresent()) {
            if (relProp.getClass().equals(RelProp.class)) {
                return Optional.of(__.start().has(property.get().getName(), ConversionUtil.convertConstraint(relProp.getCon())));
            } else if (SchematicRelProp.class.isAssignableFrom(relProp.getClass())) {
                return Optional.of(__.start().has(((SchematicRelProp)relProp).getSchematicName(),
                        ConversionUtil.convertConstraint(relProp.getCon())));
            }
        }

        return Optional.empty();
    }
    //endregion
}
