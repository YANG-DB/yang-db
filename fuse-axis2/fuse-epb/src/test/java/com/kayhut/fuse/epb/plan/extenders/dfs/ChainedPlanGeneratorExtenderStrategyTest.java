package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.ChainPlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChainedPlanGeneratorExtenderStrategyTest {
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan startPlan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 0)));


        ChainPlanExtensionStrategy chain = new ChainPlanExtensionStrategy<Plan, AsgQuery>(
                (plan, query) -> Arrays.asList(Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 1)))
                                             , Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 3)))),
                (plan, query) -> Collections.singletonList(Plan.compose(plan.get(), new RelationOp(getAsgEBaseByEnum(asgQuery, 5)))),
                (plan, query) -> Arrays.asList(Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 1)))
                                             , Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 3)))
                                             ,Plan.compose(plan.get(), new RelationOp(getAsgEBaseByEnum(asgQuery, 5)))));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 6);
        extendedPlans.forEach(p->assertEquals(p.getOps().size(),4));
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan startPlan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 0)));


        ChainPlanExtensionStrategy chain =  new ChainPlanExtensionStrategy<Plan, AsgQuery>(
                (plan, query) -> Arrays.asList(Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 1)))
                                             , Plan.compose(plan.get(), new RelationOp(getAsgEBaseByEnum(asgQuery, 3)))),
                (plan, query) -> Arrays.asList(Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 7)))
                                            , Plan.compose(plan.get(), new RelationOp(getAsgEBaseByEnum(asgQuery, 9)))),
                (plan, query) -> Arrays.asList(Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 1)))
                                             , Plan.compose(plan.get(), new EntityOp(getAsgEBaseByEnum(asgQuery, 3)))
                                             ,Plan.compose(plan.get(), new RelationOp(getAsgEBaseByEnum(asgQuery, 5)))));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 12);
        extendedPlans.forEach(p->assertEquals(p.getOps().size(),4));
    }


    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtil.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
