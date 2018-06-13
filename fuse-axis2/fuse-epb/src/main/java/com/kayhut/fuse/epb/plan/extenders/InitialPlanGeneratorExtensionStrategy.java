package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    public InitialPlanGeneratorExtensionStrategy() {
        this.planPredicate = plan -> true;
    }

    public InitialPlanGeneratorExtensionStrategy(Predicate<Plan> planPredicate) {
        this.planPredicate = planPredicate;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (plan.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<EEntityBase>> entitySeeds = AsgQueryUtil.nextDescendants(
                query.getStart(),
                asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()),
                asgEBase -> !OptionalComp.class.isAssignableFrom(asgEBase.geteBase().getClass()));

        return Stream.ofAll(entitySeeds)
                .map(entitySeed -> {
                    Optional<AsgEBase<Quant1>> entitySeedQuant = AsgQueryUtil.nextAdjacentDescendant(entitySeed, Quant1.class);
                    Optional<AsgEBase<EPropGroup>> epropGroup;
                    if (entitySeedQuant.isPresent()) {
                        epropGroup = AsgQueryUtil.nextAdjacentDescendant(entitySeedQuant.get(), EPropGroup.class);
                    } else {
                        epropGroup = AsgQueryUtil.nextAdjacentDescendant(entitySeed, EPropGroup.class);
                    }

                    Plan newPlan = new Plan(new EntityOp(entitySeed));
                    if (epropGroup.isPresent()) {
                        newPlan = newPlan.withOp(new EntityFilterOp(epropGroup.get()));
                    }

                    return newPlan;
                }).filter(this.planPredicate).toJavaList();
    }

    private Predicate<Plan> planPredicate;

}
