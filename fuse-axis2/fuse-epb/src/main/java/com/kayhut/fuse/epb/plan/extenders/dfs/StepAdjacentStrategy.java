package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

        Optional<AsgEBase<EEntityBase>> fromEntity = AsgQueryUtils.getAncestor(nextRelation.get(), EEntityBase.class);
        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtils.getNextDescendant(nextRelation.get(), EEntityBase.class);

        Plan newPlan = plan.get();
        if (newPlan.getOps().get(newPlan.getOps().size() - 1).geteNum() != fromEntity.get().geteBase().geteNum()) {
            newPlan = newPlan.withOp(new EntityOp(fromEntity.get()));
        }

        newPlan = newPlan.withOp(new RelationOp(nextRelation.get())).withOp(new EntityOp(toEntity.get()));

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

        Optional<AsgEBase<Rel>> nextRelation = getNextUnmarkedRelation(lastEntityOp.getEntity(), markedElements);
        if (!nextRelation.isPresent()) {
            Optional<AsgEBase<EEntityBase>> parentEntity = AsgQueryUtils.getAncestor(lastEntityOp.getEntity(), EEntityBase.class);
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
