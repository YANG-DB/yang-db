package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.gta.strategy.utils.EntityTranslationUtil;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.step.BoostingStepWrapper;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class EntityFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntityFilterOpTranslationStrategy(EntityTranslationOptions options) {
        super(planOp -> planOp.getClass().equals(EntityFilterOp.class));
        this.options = options;
    }
    //endregion
    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        EntityFilterOp entityFilterOp = (EntityFilterOp)planOp;

        Optional<PlanOp> previousPlanOp = PlanUtil.adjacentPrev(planWithCost.getPlan(), planOp);
        if (!previousPlanOp.isPresent()) {
            return traversal;
        }

        traversal = cleanPreviousFilteringSteps(traversal);

        EntityOp entityOp = (EntityOp)previousPlanOp.get();
        if (PlanUtil.isFirst(planWithCost.getPlan(), entityOp)) {
            traversal = appendEntityAndPropertyGroup(
                    traversal,
                    entityOp.getAsgEbase().geteBase(),
                    entityFilterOp.getAsgEbase().geteBase(),
                    context.getOnt());

        } else {
            traversal = appendPropertyGroup(
                    traversal,
                    entityOp.getAsgEbase().geteBase(),
                    entityFilterOp.getAsgEbase().geteBase(),
                    context.getOnt());
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal cleanPreviousFilteringSteps(GraphTraversal traversal) {
        TraversalUtil.remove(traversal, TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class));
        Optional<VertexStep> lastVertexStep = TraversalUtil.last(traversal, VertexStep.class);
        if (lastVertexStep.isPresent() && isFilterVertexStep(lastVertexStep.get())) {
            Optional<EdgeOtherVertexStep> nextOtherStep = TraversalUtil.next(traversal, lastVertexStep.get(), EdgeOtherVertexStep.class);
            nextOtherStep.ifPresent(edgeOtherVertexStep -> TraversalUtil.remove(traversal, edgeOtherVertexStep));
            TraversalUtil.remove(traversal, TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class));
            TraversalUtil.remove(traversal, lastVertexStep.get());
        }

        return traversal;
    }

    private GraphTraversal appendEntityAndPropertyGroup(
            GraphTraversal traversal,
            EEntityBase entity,
            EPropGroup ePropGroup,
            Ontology.Accessor ont) {

        if (entity instanceof EConcrete) {
            traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                    P.eq(Constraint.by(__.and(
                            __.has(T.id, P.eq(((EConcrete)entity).geteID())),
                            __.has(T.label, P.eq(EntityTranslationUtil.getValidEntityNames(ont, entity).get(0)))))));
        }
        else if (entity instanceof ETyped || entity instanceof EUntyped) {
            List<String> eTypeNames = EntityTranslationUtil.getValidEntityNames(ont, entity);
            Traversal constraintTraversal = __.has(T.label, P.eq(GlobalConstants.Labels.NONE));
            if (eTypeNames.size() == 1) {
                constraintTraversal = __.has(T.label, P.eq(eTypeNames.get(0)));
            } else if (eTypeNames.size() > 1) {
                constraintTraversal = __.has(T.label, P.within(eTypeNames));
            }

            List<Traversal> epropGroupTraversals = Collections.emptyList();
            if (!ePropGroup.getProps().isEmpty() || !ePropGroup.getGroups().isEmpty()) {
                epropGroupTraversals = Collections.singletonList(convertEPropGroupToTraversal(ePropGroup, ont));
            }

            if (!epropGroupTraversals.isEmpty()) {
                List<Traversal> traversals = Stream.of(constraintTraversal).appendAll(epropGroupTraversals).toJavaList();
                constraintTraversal = __.and(Stream.ofAll(traversals).toJavaArray(Traversal.class));
            }

            traversal.has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal));
        }

        return traversal;
    }

    private GraphTraversal appendPropertyGroup(
            GraphTraversal traversal,
            EEntityBase entity,
            EPropGroup ePropGroup,
            Ontology.Accessor ont) {

        List<Traversal> entityTraversals = Collections.emptyList();
        if (this.options == EntityTranslationOptions.filterEntity) {
            entityTraversals = Collections.singletonList(getEntityFilterTraversal(entity, ont));
        }

        List<Traversal> epropGroupTraversals = Collections.emptyList();
        if (!ePropGroup.getProps().isEmpty() || !ePropGroup.getGroups().isEmpty()) {
            epropGroupTraversals = Collections.singletonList(convertEPropGroupToTraversal(ePropGroup, ont));
        }

        List<Traversal> traversals = Stream.ofAll(entityTraversals).appendAll(epropGroupTraversals).toJavaList();
        if (traversals.isEmpty()) {
            return traversal;
        }

        Traversal constraintTraversal = traversals.size() == 1 ?
                traversals.get(0) :
                __.and(Stream.ofAll(traversals).toJavaArray(Traversal.class));

        Stream.ofAll(TraversalUtil.<Step>lastSteps(traversal, step -> step.getLabels().contains(entity.geteTag())))
                .forEach(step -> step.removeLabel(entity.geteTag()));

        return traversal.outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal))
                .otherV().as(entity.geteTag());
    }

    private Traversal getEntityFilterTraversal(EEntityBase entity, Ontology.Accessor ont) {
        if (entity instanceof EConcrete) {
            //traversal.has(GlobalConstants.HasKeys.PROMISE, P.eq(Promise.as(((EConcrete) entity).geteID())));
            return __.has(T.id, P.eq(((EConcrete)entity).geteID()));
        }
        else if (entity instanceof ETyped || entity instanceof EUntyped) {
            List<String> eTypeNames = EntityTranslationUtil.getValidEntityNames(ont, entity);
            if (eTypeNames.isEmpty()) {
                return __.has(T.label, P.eq(GlobalConstants.Labels.NONE));
            } else if (eTypeNames.size() == 1) {
                return __.has(T.label, P.eq(eTypeNames.get(0)));
            } else if (eTypeNames.size() > 1) {
                return __.has(T.label, P.within(eTypeNames));
            }
        }

        return null;
    }

    private Traversal convertEPropGroupToTraversal(EPropGroup ePropGroup, Ontology.Accessor ont) {
        List<Traversal> childGroupTraversals = Stream.ofAll(ePropGroup.getGroups())
                .map(childGroup -> convertEPropGroupToTraversal(childGroup, ont))
                .toJavaList();

        List<Traversal> epropTraversals = Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .map(eprop -> convertEPropToTraversal(eprop, ont))
                .toJavaList();


        Stream<Traversal> traversalStream = Stream.ofAll(epropTraversals).appendAll(childGroupTraversals);

        Traversal[] traversals = traversalStream.toJavaArray(Traversal.class);
        Traversal ret;
        if (traversals.length == 1) {
            ret = traversals[0];
        }
        else {
            switch (ePropGroup.getQuantType()) {
                case all:
                    ret = __.and(traversals);
                    break;
                case some:
                    ret = __.or(traversals);
                    break;
                default:
                    ret = __.and(traversals);
            }
        }

        if(ePropGroup instanceof RankingProp){
            GraphTraversal.Admin admin = __.start().asAdmin();
            ret = admin.addStep(new BoostingStepWrapper(ret.asAdmin().getEndStep(), ((RankingProp) ePropGroup).getBoost()));
        }
        return ret;
    }

    private Traversal convertEPropToTraversal(EProp eProp, Ontology.Accessor ont) {
        Optional<Property> property = ont.$property(eProp.getpType());
        if (!property.isPresent()) {
            return __.start();
        }

        String actualPropertyName = SchematicEProp.class.isAssignableFrom(eProp.getClass()) ?
                ((SchematicEProp)eProp).getSchematicName() : property.get().getName();

        GraphTraversal<Object, Object> traversal = __.has(actualPropertyName, ConversionUtil.convertConstraint(eProp.getCon()));
        if(eProp instanceof RankingProp){
            GraphTraversal.Admin admin = __.start().asAdmin();
            traversal = admin.addStep(new BoostingStepWrapper<>(traversal.asAdmin().getEndStep(), ((RankingProp) eProp).getBoost()));
        }
        return traversal;

    }

    private boolean isFilterVertexStep(VertexStep vertexStep) {
        return !Stream.of(vertexStep.getEdgeLabels())
                .filter(edgeLabel -> edgeLabel.equals(GlobalConstants.Labels.PROMISE_FILTER))
                .isEmpty();

    }
    //endregion

    //region Fields
    private EntityTranslationOptions options;
    //endregion
}
