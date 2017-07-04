package com.kayhut.fuse.epb.plan.seeders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanSeedStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorSeedStrategy implements PlanSeedStrategy<Plan, AsgQuery> {
    @Inject
    public InitialPlanGeneratorSeedStrategy() {}

    @Override
    public Iterable<Plan> extendPlan(AsgQuery query) {
        List<Plan> plans = new LinkedList<>();
        List<AsgEBase<EEntityBase>> entitySeeds = AsgQueryUtil.nextDescendants(query.getStart(), e -> e.geteBase() instanceof EEntityBase, p -> true);
        entitySeeds.forEach(entity-> {
            Optional<AsgEBase<EPropGroup>> epropGroup = AsgQueryUtil.nextAdjacentDescendant(entity, EPropGroup.class, 2);
            EntityOp op = new EntityOp(entity);
            Plan newPlan = new Plan(Collections.singletonList(op));
            if(epropGroup.isPresent()) {
                newPlan = Plan.compose(newPlan,new EntityFilterOp(epropGroup.get()));
                newPlan.log("InitialPlanGeneratorSeedStrategy:[empty->"+newPlan.getOps().size()+"]", Level.INFO);
            }
            plans.add(newPlan);
        });

        return plans;
    }

}
