package com.kayhut.fuse.epb.plan.extenders.M1;

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsNonRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    public M1DfsNonRedundantPlanExtensionStrategy() {
        super();

        this.innerExtenders = Stream.<PlanExtensionStrategy<Plan, AsgQuery>>of(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new InitialPlanGeneratorExtensionStrategy(),
                                new StepAdjacentDfsStrategy(),
                                new OptionalOpExtensionStrategy(this)
                        ),
                        new OptionalInitialExtensionStrategy()
                )
        ).toJavaList();
    }
    //endregion
}
