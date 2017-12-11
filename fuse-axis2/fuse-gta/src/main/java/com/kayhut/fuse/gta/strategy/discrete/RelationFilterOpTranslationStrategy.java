package com.kayhut.fuse.gta.strategy.discrete;

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
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Optional;

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
        List<Traversal> traversals = Stream.ofAll(relPropGroup.getProps())
                .map(relProp -> convertRelPropToTraversal(relProp, ont))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        traversals.add(0, __.has(T.label, P.eq(relationTypeName)));

        return traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class))));
    }
    //endregion

    //region Private Methods
    private Optional<Traversal> convertRelPropToTraversal(RelProp relProp, Ontology.Accessor ont) {
        Optional<Property> property = ont.$property(relProp.getpType());
        if (property.isPresent()) {
            if (relProp.getClass().equals(RelProp.class)) {
                return Optional.of(__.has(property.get().getName(), ConversionUtil.convertConstraint(relProp.getCon())));
            } else if (relProp.getClass().equals(RedundantRelProp.class)) {
                return Optional.of(__.has(((RedundantRelProp)relProp).getRedundantPropName(),
                        ConversionUtil.convertConstraint(relProp.getCon())));
            }
        }

        return Optional.empty();
    }
    //endregion
}
