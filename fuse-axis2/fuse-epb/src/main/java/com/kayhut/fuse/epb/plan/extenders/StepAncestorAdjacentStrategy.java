package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;

import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getLastOpOfType;
import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextAncestorOfType;
import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextAncestorUnmarkedOfType;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAncestorAdjacentStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<Rel>> nextRelation = getNextAncestorOfType(plan.get(), Rel.class);
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }
        //reverse direction
        //nextRelation.get().geteBase().setDir(Direction.reverse(nextRelation.get().geteBase().getDir()));
        //
        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.getBDescendant(nextRelation.get(), RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.getAncestor(nextRelation.get(), EEntityBase.class);
        if (!toEntity.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.getNextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = toEntityQuant.isPresent() ?
                AsgQueryUtil.getNextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class) :
                AsgQueryUtil.getNextAdjacentDescendant(toEntity.get(), EPropGroup.class);


        Plan newPlan = Plan.clone(plan.get());
        //current step on plan is the "from" entity whether is entity or filter op
        RelationOp relationOp = new RelationOp(nextRelation.get());
        relationOp.getAsgEBase().geteBase().setDir(Direction.reverse(nextRelation.get().geteBase().getDir()));
        newPlan = newPlan.withOp(relationOp);
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }
        //to entity step
        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }

        if(!Plan.equals(plan.get(), newPlan)) {
            newPlan.log("StepAncestorAdjacentStrategy:[" + Plan.diff(plan.get(), newPlan) + "]", Level.INFO);
        }
        return Collections.singletonList(newPlan);
    }
    //endregion

}
