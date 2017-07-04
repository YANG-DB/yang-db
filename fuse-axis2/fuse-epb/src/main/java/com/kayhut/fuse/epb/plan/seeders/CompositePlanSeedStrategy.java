package com.kayhut.fuse.epb.plan.seeders;

import com.kayhut.fuse.epb.plan.PlanSeedStrategy;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import javaslang.collection.Stream;

/**
 * Created by Roman on 04/07/2017.
 */
public class CompositePlanSeedStrategy<P extends IPlan, Q extends IQuery>  implements PlanSeedStrategy<P, Q> {
    //region Constructors
    public CompositePlanSeedStrategy(PlanSeedStrategy<P, Q>...planSeeders) {
        this.planSeeders = Stream.of(planSeeders).toJavaList();
    }

    public CompositePlanSeedStrategy(Iterable<PlanSeedStrategy<P, Q>> planSeeders) {
        this.planSeeders = Stream.ofAll(planSeeders).toJavaList();
    }
    //endregion

    //region PlanSeedStrategy Implementation
    @Override
    public Iterable<P> extendPlan(Q query) {
        return Stream.ofAll(planSeeders)
                .map(planSeeder -> planSeeder.extendPlan(query))
                .flatMap(Stream::ofAll)
                .toJavaList();
    }
    //endregion

    //region Fields
    private Iterable<PlanSeedStrategy<P, Q>> planSeeders;
    //endregion
}
