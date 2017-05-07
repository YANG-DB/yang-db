package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.ChainPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ChainedPlanGeneratorExtenderStrategyTest {
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");

        Plan startPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 0)));
        Plan expectedPlan1 = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        Plan expectedPlan2 = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        ChainPlanExtensionStrategy chain = new ChainPlanExtensionStrategy(
                (plan, query) -> Collections.singletonList(expectedPlan1),
                (plan, query) -> Collections.singletonList(expectedPlan2));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        Plan actualPlan1 = extendedPlans.get(0);
        assertEquals(actualPlan1.getOps().size(),3);
    }


    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
