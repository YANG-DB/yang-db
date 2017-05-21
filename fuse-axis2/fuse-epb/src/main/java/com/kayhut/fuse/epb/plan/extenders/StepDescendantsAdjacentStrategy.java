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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextDescendantsUnmarkedOfType;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepDescendantsAdjacentStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<Rel>> nextRelations = getNextDescendantsUnmarkedOfType(plan.get(), Rel.class);

        if (nextRelations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Plan> plans = new ArrayList<>();

        Plan newPlan = plan.get();
        for (AsgEBase<Rel> nextRelation : nextRelations) {
            Optional<Plan> computedPlan = compute(nextRelation, newPlan);
            if (computedPlan.isPresent()) {
                plans.add(computedPlan.get());

                if (!Plan.equals(plan.get(), newPlan)) {
                    newPlan.log("StepDescendantsAdjacentStrategy:[" + Plan.diff(plan.get(), newPlan) + "]", Level.INFO);
                }
            }
        }

        return plans;
    }

    private Optional<Plan> compute(AsgEBase<Rel> nextRelation, Plan newPlan) {

        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.getBDescendant(nextRelation, RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.getNextDescendant(nextRelation, EEntityBase.class);
        if (!toEntity.isPresent()) {
            return Optional.empty();
        }

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.getNextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = toEntityQuant.isPresent() ?
                AsgQueryUtil.getNextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class) :
                AsgQueryUtil.getNextAdjacentDescendant(toEntity.get(), EPropGroup.class);

        newPlan = newPlan.withOp(new RelationOp(nextRelation));
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }

        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }

        return Optional.of(newPlan);
    }
    //endregion

}
