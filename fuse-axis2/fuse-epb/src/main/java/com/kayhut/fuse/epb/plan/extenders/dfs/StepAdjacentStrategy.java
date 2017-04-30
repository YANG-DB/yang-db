package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAdjacentStrategy implements PlanExtensionStrategy<Plan,AsgQuery> {
    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<Rel>> nextRelation = getNextUnmarkedRelation(plan.get());
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtils.getBDescendant(nextRelation.get(), RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> fromEntity = AsgQueryUtils.getAncestor(nextRelation.get(), EEntityBase.class);
        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtils.getNextDescendant(nextRelation.get(), EEntityBase.class);

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtils.getNextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = Optional.empty();
        if (toEntityQuant.isPresent()) {
            toEntityPropGroup = AsgQueryUtils.getNextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class);
        }

        Plan newPlan = plan.get();
        if (getLastEntityOp(newPlan).geteNum() != fromEntity.get().geteNum()) {
            newPlan = newPlan.withOp(new GoToEntityOp(fromEntity.get()));
        }

        newPlan = newPlan.withOp(new RelationOp(nextRelation.get()));
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }

        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }


        return Collections.singletonList(newPlan);
    }
    //endregion

    //region Private Methods
    private EntityOp getLastEntityOp(Plan plan) {
        EntityOp lastEntityOp = null;
        for(int i = plan.getOps().size() - 1 ; i >= 0 ; i--) {
            PlanOpBase planOp = plan.getOps().get(i);
            if (planOp instanceof EntityOp) {
                lastEntityOp = (EntityOp)planOp;
                break;
            }
        }

        return lastEntityOp;
    }

    private Set<Integer> markEntitiesAndRelations(Plan plan) {
        return Stream.ofAll(plan.getOps()).map(op -> op.geteNum()).toJavaSet();
    }

    private <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextUnmarkedRelation(
            AsgEBase<T> asgEBase,
            Set<Integer> markedElements) {
        return AsgQueryUtils.getNextDescendant(asgEBase,
                        (child) -> Rel.class.isAssignableFrom(child.geteBase().getClass()) &&
                                    !markedElements.contains(child.geteNum()));
    }

    private Optional<AsgEBase<Rel>> getNextUnmarkedRelation(Plan plan) {
        Set<Integer> markedElements = markEntitiesAndRelations(plan);
        EntityOp lastEntityOp = getLastEntityOp(plan);

        Optional<AsgEBase<Rel>> nextRelation = getNextUnmarkedRelation(lastEntityOp.getAsgEBase(), markedElements);
        if (!nextRelation.isPresent()) {
            Optional<AsgEBase<EEntityBase>> parentEntity = AsgQueryUtils.getAncestor(lastEntityOp.getAsgEBase(), EEntityBase.class);
            while(parentEntity.isPresent()) {
                nextRelation = getNextUnmarkedRelation(parentEntity.get(), markedElements);
                if (nextRelation.isPresent()) {
                    break;
                }

                parentEntity = AsgQueryUtils.getAncestor(parentEntity.get(), EEntityBase.class);
            }
        }

        return nextRelation;
    }
    //endregion
}
