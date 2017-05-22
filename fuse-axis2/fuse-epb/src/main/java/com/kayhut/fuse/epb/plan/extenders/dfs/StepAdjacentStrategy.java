package com.kayhut.fuse.epb.plan.extenders.dfs;

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

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getLastOpOfType;
import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextDescendantUnmarkedOfType;

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

        Optional<AsgEBase<Rel>> nextRelation = getNextDescendantUnmarkedOfType(plan.get(),Rel.class);
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.bDescendant(nextRelation.get(), RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> fromEntity = AsgQueryUtil.ancestor(nextRelation.get(), EEntityBase.class);
        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.nextDescendant(nextRelation.get(), EEntityBase.class);

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = Optional.empty();
        if (toEntityQuant.isPresent()) {
            toEntityPropGroup = AsgQueryUtil.nextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class);
        } else {
            toEntityPropGroup = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), EPropGroup.class);
        }

        Plan newPlan = plan.get();
        if (getLastOpOfType(newPlan,EntityOp.class).geteNum() != fromEntity.get().geteNum()) {
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

}
