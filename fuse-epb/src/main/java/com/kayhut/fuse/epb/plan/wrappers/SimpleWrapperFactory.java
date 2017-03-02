package com.kayhut.fuse.epb.plan.wrappers;

import com.kayhut.fuse.epb.plan.PlanWrapper;
import com.kayhut.fuse.epb.plan.PlanWrapperFactory;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;

import java.util.Map;

/**
 * Created by moti on 2/28/2017.
 */
public class SimpleWrapperFactory implements PlanWrapperFactory<Plan, AsgQuery ,SingleCost> {
    @Override
    public PlanWrapper<Plan, SingleCost> wrapPlan(Plan extendedPlan, AsgQuery query) {

        Map<Integer, EBaseAsg> queryParts = SimpleExtenderUtils.flattenQuery(query);
        SimpleExtenderUtils.removeHandledParts(extendedPlan, queryParts);
        boolean isComplete = queryParts.isEmpty();

        PlanWrapper<Plan, SingleCost> wrapper = new PlanWrapper<Plan, SingleCost>() {
            @Override
            public Plan getPlan() {
                return extendedPlan;
            }

            @Override
            public SingleCost getPlanCost() {
                // TODO: implement guice creation with cost injection
                return new SingleCost(0);
            }

            @Override
            public boolean isPlanComplete() {
                return isComplete;
            }
        };

        return wrapper;
    }
}
