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

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextAncestorOfType;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAncestorAdjacentStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {

        Optional<AsgEBase<Rel>> nextRelation = getNextAncestorOfType(plan, Rel.class);
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }
        //reverse direction
        //nextRelation.get().geteBase().setDir(Direction.reverse(nextRelation.get().geteBase().getDir()));
        //
        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.bDescendant(nextRelation.get(), RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.ancestor(nextRelation.get(), EEntityBase.class);
        if (!toEntity.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = toEntityQuant.isPresent() ?
                AsgQueryUtil.nextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class) :
                AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), EPropGroup.class);


        Plan newPlan = Plan.clone(plan);
        //current pattern on plan is the "from" entity whether is entity or filter op
        RelationOp relationOp = new RelationOp(nextRelation.get(), Direction.reverse(nextRelation.get().geteBase().getDir()));
        newPlan = newPlan.withOp(relationOp);
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }
        //to entity pattern
        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }

        if(!Plan.equals(plan, newPlan)) {
            newPlan.log("StepAncestorAdjacentStrategy:[" + Plan.diff(plan, newPlan) + "]", Level.INFO);
        }
        return Collections.singletonList(newPlan);
    }
    //endregion

}
