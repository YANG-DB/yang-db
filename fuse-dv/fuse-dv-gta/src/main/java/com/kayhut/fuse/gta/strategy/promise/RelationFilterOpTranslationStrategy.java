package com.kayhut.fuse.gta.strategy.promise;

/*-
 * #%L
 * fuse-dv-gta
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

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RedundantRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.SchematicRelProp;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class RelationFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationFilterOpTranslationStrategy() {
        super(RelationFilterOp.class);
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
        List<Traversal> traversals = Stream.ofAll(relPropGroup.getProps())
                .filter(relProp -> relProp.getCon() != null)
                .map(relProp -> convertRelPropToTraversal(relProp, ont))
                .toJavaList();

        traversals.addAll(0, Arrays.asList(
                __.has(T.label, P.eq(relationTypeName)),
                __.has(GlobalConstants.HasKeys.DIRECTION, P.eq(ConversionUtil.convertDirection(rel.getDir())))
        ));

        return traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class))));
    }

    private Traversal convertRelPropToTraversal(RelProp relProp, Ontology.Accessor ont) {
        Optional<Property> property = ont.$property(relProp.getpType());
        return property.<Traversal>map(property1 ->
                __.has(SchematicRelProp.class.isAssignableFrom(relProp.getClass()) ?
                        ((SchematicRelProp)relProp).getSchematicName() :
                        property1.getName()
                , ConversionUtil.convertConstraint(relProp.getCon())))
                .orElseGet(__::start);

    }
    //endregion
}
