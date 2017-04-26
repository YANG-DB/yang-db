package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.entity.EConcrete;
import javaslang.Tuple2;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.getSupportedPattern;
import static com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder.mock;
import static com.kayhut.fuse.epb.tests.PlanMockUtils.Type.CONCRETE;
import static com.kayhut.fuse.epb.tests.PlanMockUtils.Type.TYPED;
import static com.kayhut.fuse.epb.tests.StatisticsMockUtils.build;
import static com.kayhut.fuse.model.execution.plan.Direction.out;

/**
 * Created by moti on 4/19/2017.
 */
public class StatisticalCostEstimatorTests {


    @Test
    public void planEstimatorPatternTwoTest(){

        StatisticsCostEstimator.StatisticsCostEstimatorPatterns[] supportedPattern = getSupportedPattern();

        String s1 = new Plan().withOp(new EntityOp()).toPattern();

        String s2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).toPattern();

        String s3 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationFilterOp()).toPattern();

        Pattern compileP2 = Pattern.compile(supportedPattern[1].pattern());

        Matcher matcher = compileP2.matcher(s1);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOnly"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOnlyFilter"),null);
        }

        matcher = compileP2.matcher(s2);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOnly"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOnlyFilter"),"EntityFilterOp");
        }

        matcher = compileP2.matcher(s3);
        Assert.assertFalse(matcher.matches());
    }

    @Test
    public void planEstimatorPatternOneTest(){
        StatisticsCostEstimator.StatisticsCostEstimatorPatterns[] supportedPattern = getSupportedPattern();

        String s1 = new Plan().withOp(new EntityOp()).toPattern();

        String s2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).toPattern();

        String s3 = new Plan().withOp(new EntityOp()).withOp(new RelationOp()).withOp(new EntityOp()).toPattern();

        String s4 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationOp()).withOp(new RelationFilterOp()).withOp(new EntityOp()).withOp(new EntityFilterOp()).toPattern();


        Pattern compileP1 = Pattern.compile(supportedPattern[0].pattern());

        Matcher matcher = compileP1.matcher(s1);
        Assert.assertFalse(matcher.matches());

        matcher = compileP1.matcher(s2);
        Assert.assertFalse(matcher.matches());

        matcher = compileP1.matcher(s3);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOne"),"EntityOp");
            Assert.assertEquals(matcher.group("relation"),"RelationOp");
            Assert.assertEquals(matcher.group("entityTwo"),"EntityOp");
        }

        matcher = compileP1.matcher(s4);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOne"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOneFilter"),"EntityFilterOp:");
            Assert.assertEquals(matcher.group("relation"),"RelationOp");
            Assert.assertEquals(matcher.group("optionalRelFilter"),"RelationFilterOp:");
            Assert.assertEquals(matcher.group("entityTwo"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityTwoFilter"),"EntityFilterOp");
        }


    }

    @Test
    public void calculateEntityOnlyPattern() throws Exception {
        StatisticsCostEstimator estimator = new StatisticsCostEstimator(build(Collections.emptyMap(),Integer.MAX_VALUE,Integer.MAX_VALUE));

        HashMap<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map = new HashMap<>();
        EntityOp entityOp = new EntityOp();
        entityOp.setEntity(new AsgEBase<>(new EConcrete()));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONLY, entityOp);
        Tuple2<Double, List<PlanOpWithCost<Cost>>> tuple2 = estimator.calculate(map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns.SINGLE_MODE, Optional.empty());
        List<PlanOpWithCost<Cost>> costs = tuple2._2;

        Assert.assertNotNull(costs);
        Assert.assertEquals(costs.size(),1);
        Assert.assertEquals(costs.get(0).getOpBase().size(),2);
        Assert.assertTrue(costs.get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertTrue(costs.get(0).getOpBase().get(1) instanceof EntityFilterOp);
        Assert.assertEquals(costs.get(0).getCost().cost,1,0);

    }

    @Test
    public void calculateStepPattern() throws Exception {
        PlanMockBuilder builder = mock().entity(TYPED, 100, 4).entityFilter(0.2,"filter1".hashCode()).startNewPlan().rel(out, 5, 100).relFilter(0.6,"filter2".hashCode()).entity(CONCRETE, 1, 5).entityFilter(1,"filter3".hashCode());
        StatisticsCostEstimator estimator = new StatisticsCostEstimator(build(builder.statistics(),Integer.MAX_VALUE,Integer.MAX_VALUE));

        Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost = Optional.of(builder.planWithCost(50, 250));
        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(builder.plan(), previousCost);

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(),6);

        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().size(),1);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(1).getOpBase().get(0)  instanceof EntityFilterOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(1).getOpBase().size(),1);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().size(),2);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().get(0) instanceof RelationOp);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().size(),2);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().get(0) instanceof EntityOp);

        Assert.assertTrue(estimate.getCost().getGlobalCost().equals(new Cost(111.0,0)));


    }

    @Test
    public void estimateEntityOnlyPattern() throws Exception {
        StatisticsCostEstimator estimator = new StatisticsCostEstimator(build(Collections.emptyMap(),Integer.MAX_VALUE,Integer.MAX_VALUE));
        EntityOp entityOp = new EntityOp();
        entityOp.setEntity(new AsgEBase<>(new EConcrete()));

        Plan plan = new Plan().withOp(entityOp);
        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(plan, Optional.empty());

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(),2);
        Assert.assertEquals(estimate.getCost().getGlobalCost().cost,1,0);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).size(),1);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().size(),2);
    }

}