package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextUnmarkedOfType;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepExpanderStrategy implements PlanExtensionStrategy<Plan,AsgQuery> {
    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            //plan initiation
            //start next always is a single element of type EEntityBase (todo validate start step ?)
            AsgEBase<? extends EBase> eBase = query.getStart().getNext().get(0);
            if(eBase.geteBase() instanceof EEntityBase) {
                //if start has below - add this to plan
                if(!eBase.getB().isEmpty()) {

                }
                return Collections.singletonList(new Plan().withOp(new EntityOp((AsgEBase<EEntityBase>) eBase)));
            }
        }

        Optional<AsgEBase<Rel>> nextRelation = getNextUnmarkedOfType(plan.get(),EBase.class);
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

    //endregion
}
