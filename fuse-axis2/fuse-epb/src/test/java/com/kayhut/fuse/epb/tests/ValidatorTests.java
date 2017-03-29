package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.validation.SiblingOnlyPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 3/5/2017.
 */
public class ValidatorTests {

    @Test
    public void SiblingValidatorLegalPlanTest(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> pathQuery = BuilderTestUtil.createTwoEntitiesPathQuery();
        Plan<SingleCost> plan = BuilderTestUtil.createPlanForTwoEntitiesPathQuery(pathQuery.getKey());
        SiblingOnlyPlanValidator<SingleCost> validator = new SiblingOnlyPlanValidator<>();
        Assert.assertTrue(validator.isPlanValid(plan, pathQuery.getLeft()));
    }

    @Test
    public void SiblingValidatorNotLegalPlanTest(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> pathQuery = BuilderTestUtil.createTwoEntitiesPathQuery();
        Plan<SingleCost> plan = BuilderTestUtil.createPlanForTwoEntitiesPathQuery(pathQuery.getKey());
        List<PlanOpWithCost<SingleCost>> ops = new LinkedList<>();
        ops.add(plan.getOps().get(0));
        ops.add(plan.getOps().get(2));
        ops.add(plan.getOps().get(1));
        SiblingOnlyPlanValidator<SingleCost> validator = new SiblingOnlyPlanValidator<>();
        Assert.assertFalse(validator.isPlanValid(new Plan<SingleCost>(ops), pathQuery.getLeft()));
    }


}
