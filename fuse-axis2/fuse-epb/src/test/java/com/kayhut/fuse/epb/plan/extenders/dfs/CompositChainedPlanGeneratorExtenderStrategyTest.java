package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class CompositChainedPlanGeneratorExtenderStrategyTest {

    @Test
    @Ignore
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");

        Plan startPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 0)));
        Plan expectedPlan1 = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        Plan expectedPlan2 = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        StepAncestorAdjacentStrategy ancestor = new StepAncestorAdjacentStrategy();
        StepDescendantsAdjacentStrategy descendant = new StepDescendantsAdjacentStrategy();
        CompositePlanExtensionStrategy compositeIntern = new CompositePlanExtensionStrategy(ancestor,descendant);

        GotoExtensionStrategy goToExtender = new GotoExtensionStrategy();
        ChainPlanExtensionStrategy chain = new ChainPlanExtensionStrategy(goToExtender, compositeIntern);
        ChainPlanExtensionStrategy composite = new ChainPlanExtensionStrategy(ancestor,descendant,chain);

        List<Plan> extendedPlans = Stream.ofAll(composite.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

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
