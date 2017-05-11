package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by Roman on 23/04/2017.
 */
public class InitialPlanGeneratorExtenderStrategyTest {
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan expectedPlan1 = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        Plan expectedPlan2 = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanGeneratorExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),2);
        Plan actualPlan1 = extendedPlans.get(0);
        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
        Plan actualPlan2 = extendedPlans.get(1);
        PlanAssert.assertEquals(expectedPlan2, actualPlan2);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanGeneratorExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),4);

        assertEquals(extendedPlans.get(0).getOps().size(), 1);
        assertEquals(extendedPlans.get(1).getOps().size(), 2);
        assertEquals(extendedPlans.get(2).getOps().size(), 1);
        assertEquals(extendedPlans.get(3).getOps().size(), 1);
    }

    @Test
    public void test_simpleQuery3_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery3("name", "ont");
        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanGeneratorExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),6);

        assertEquals(extendedPlans.get(0).getOps().size(), 1);
        assertEquals(extendedPlans.get(1).getOps().size(), 2);
        assertEquals(extendedPlans.get(2).getOps().size(), 1);
        assertEquals(extendedPlans.get(3).getOps().size(), 1);
        assertEquals(extendedPlans.get(4).getOps().size(), 1);
        assertEquals(extendedPlans.get(5).getOps().size(), 1);
    }


    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
