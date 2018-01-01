package com.kayhut.fuse.epb.plan.extenders.M1;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

import java.util.Optional;

/**
 * Created by Roman on 21/05/2017.
 */
public class M1NonRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1NonRedundantPlanExtensionStrategy() {
        super(
                new CompositePlanExtensionStrategy<>(
                        new InitialPlanGeneratorExtensionStrategy(),
                        //new StepAncestorAdjacentStrategy(),
                        //new StepDescendantsAdjacentStrategy(),
                        new ChainPlanExtensionStrategy<>(
                                new GotoExtensionStrategy(true),
                                new CompositePlanExtensionStrategy<>(
                                        new StepAncestorAdjacentStrategy(),
                                        new StepDescendantsAdjacentStrategy()
                                )
                        )
                )
        );
    }
    //endregion

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        return super.extendPlan(plan, query);
    }
}
