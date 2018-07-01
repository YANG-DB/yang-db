package com.kayhut.fuse.epb.plan.modules;

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.typesafe.config.Config;

/**
 * Created by Roman on 22/05/2017.
 */
public class EpbDfsRedundantModule extends BaseEpbModule {
    //region Private Methods
    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config config) {
        return M1DfsRedundantPlanExtensionStrategy.class;
    }
    //endregion
}
