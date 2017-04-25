package com.kayhut.fuse.epb.tests;

import com.google.common.collect.Lists;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 4/19/2017.
 */
public class StatisticalCostEstimatorTests {

    StatisticsCostEstimator estimator;

    @Before
    public void setup() {
        StatisticsProvider mock = Mockito.mock(StatisticsProvider.class);
        //elastic statistics provider
        estimator = new StatisticsCostEstimator(mock);
        //mock statistics provider
        when(mock.getStatistics(any())).thenReturn(new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<String>(1l,1l,"a","z"))));
        when(mock.getRedundantStatistics(any())).thenReturn(new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<String>(1l,1l,"a","z"))));
        when(mock.getRedundantStatistics(any(),any(),any())).thenReturn(new Statistics.HistogramStatistics(Collections.singletonList(new Statistics.BucketInfo<String>(1l,1l,"a","z"))));
    }

    @Test
    public void planEstimatorPatternTwoTest(){
        StatisticsCostEstimator.StatisticsCostEstimatorPatterns[] supportedPattern = estimator.getSupportedPattern();

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
        StatisticsCostEstimator.StatisticsCostEstimatorPatterns[] supportedPattern = estimator.getSupportedPattern();

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
        HashMap<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map = new HashMap<>();
        EntityOp entityOp = new EntityOp();
        entityOp.setEntity(new AsgEBase<>(new EConcrete()));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONLY, entityOp);
        List<PlanOpWithCost<Cost>> costs = estimator.calculate(map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns.SINGLE_MODE, Optional.empty());

        Assert.assertNotNull(costs);
        Assert.assertEquals(costs.size(),1);
        Assert.assertEquals(costs.get(0).getOpBase().size(),2);
        Assert.assertTrue(costs.get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertTrue(costs.get(0).getOpBase().get(1) instanceof EntityFilterOp);
        Assert.assertEquals(costs.get(0).getCost().cost,1,0);

    }

    @Test
    public void calculateStepPattern() throws Exception {
        HashMap<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map = new HashMap<>();

        EntityOp entityOneOp = new EntityOp();
        entityOneOp.setEntity(new AsgEBase<>(new EConcrete()));
        EntityFilterOp filterOneOp = new EntityFilterOp();

        RelationOp relationOp = new RelationOp();
        Rel rel = new Rel();
        rel.setDir(Direction.out.name());
        relationOp.setRelation(new AsgEBase<>(rel));
        RelationFilterOp relationFilterOp = new RelationFilterOp();

        EntityOp entityTwoOp = new EntityOp();
        entityTwoOp.setEntity(new AsgEBase<>(new EConcrete()));

        Plan plan = new Plan().withOp(entityOneOp).withOp(filterOneOp).withOp(relationOp).withOp(relationFilterOp).withOp(entityTwoOp);

        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONE, entityOneOp);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_ENTITY_ONLY_FILTER, filterOneOp);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.RELATION, relationOp);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_REL_FILTER, relationFilterOp);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_TWO, entityTwoOp);

        Cost cost = Cost.of(1, 1);
        List<PlanOpWithCost<Cost>> opCosts = Arrays.asList(PlanOpWithCost.of(cost,1, entityOneOp),PlanOpWithCost.of(cost,1, filterOneOp ),
                PlanOpWithCost.of(cost,1, relationOp),PlanOpWithCost.of(cost,1 ,relationFilterOp),PlanOpWithCost.of(cost,1, entityTwoOp));

        PlanDetailedCost planDetailedCost = new PlanDetailedCost(cost, opCosts);
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = new PlanWithCost<>(plan, planDetailedCost);

        List<PlanOpWithCost<Cost>> costs = estimator.calculate(map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns.FULL_STEP, Optional.of(planWithCost));

        Assert.assertNotNull(costs);
        Assert.assertEquals(costs.size(),3);
        Assert.assertEquals(costs.get(0).getOpBase().size(),2);
        Assert.assertTrue(costs.get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertTrue(costs.get(1).getOpBase().get(0)  instanceof RelationOp);
        Assert.assertEquals(costs.get(1).getOpBase().size(),2);
        Assert.assertTrue(costs.get(2).getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(costs.get(2).getOpBase().size(),2);

        Assert.assertEquals(costs.get(0).getCost().cost,1,0);


    }

    @Test
    public void estimateEntityOnlyPattern() throws Exception {
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

    @Test
    public void estimateStepPattern() throws Exception {
        EntityOp entityOneOp = new EntityOp();
        entityOneOp.setEntity(new AsgEBase<>(new EConcrete()));
        EntityFilterOp filterOneOp = new EntityFilterOp();

        RelationOp relationOp = new RelationOp();
        Rel rel = new Rel();
        rel.setDir(Direction.out.name());
        relationOp.setRelation(new AsgEBase<>(rel));
        RelationFilterOp relationFilterOp = new RelationFilterOp();

        EntityOp entityTwoOp = new EntityOp();
        entityTwoOp.setEntity(new AsgEBase<>(new EConcrete()));

        Plan planOld = new Plan().withOp(entityOneOp).withOp(filterOneOp);
        Plan plan = new Plan().withOp(entityOneOp).withOp(filterOneOp).withOp(relationOp).withOp(relationFilterOp).withOp(entityTwoOp);

        Cost cost = Cost.of(1, 1);
        List<PlanOpWithCost<Cost>> opCosts = Arrays.asList(PlanOpWithCost.of(cost,1, entityOneOp), PlanOpWithCost.of(cost,1, filterOneOp ));
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = new PlanWithCost<>(planOld, new PlanDetailedCost(cost, opCosts));

        PlanWithCost<Plan, PlanDetailedCost> estimate = estimator.estimate(plan, Optional.of(planWithCost));

        Assert.assertNotNull(estimate);
        Assert.assertEquals(estimate.getPlan().getOps().size(),6);

        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(0).getOpBase().size(),1);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(1).getOpBase().get(0)  instanceof EntityFilterOp);
        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(1).getOpBase().size(),1);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().size(),2);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().get(0) instanceof RelationOp);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(2).getOpBase().get(1) instanceof RelationFilterOp);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().size(),2);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().get(0) instanceof EntityOp);
        Assert.assertTrue(newArrayList(estimate.getCost().getOpCosts()).get(3).getOpBase().get(1) instanceof EntityFilterOp);

        Assert.assertEquals(newArrayList(estimate.getCost().getOpCosts()).get(0).getCost().cost,1,0);
    }
}
