package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }

    @Test
    public void planEstimatorPatternOneTest(){
        List<String> supportedPattern = estimator.getSupportedPattern();

        String s1 = new Plan().withOp(new EntityOp()).typePattern();

        String s2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).typePattern();

        String s3 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationFilterOp()).typePattern();

        Pattern compileP1 = Pattern.compile(supportedPattern.get(0));

        Matcher matcher = compileP1.matcher(s1);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOnly"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOnlyFilter"),null);
        }

        matcher = compileP1.matcher(s2);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOnly"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOnlyFilter"),"EntityFilterOp");
        }

        matcher = compileP1.matcher(s3);
        Assert.assertFalse(matcher.matches());
    }

    @Test
    public void planEstimatorPatternTwoTest(){
        List<String> supportedPattern = estimator.getSupportedPattern();

        String s1 = new Plan().withOp(new EntityOp()).typePattern();

        String s2 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).typePattern();

        String s3 = new Plan().withOp(new EntityOp()).withOp(new RelationOp()).withOp(new EntityOp()).typePattern();

        String s4 = new Plan().withOp(new EntityOp()).withOp(new EntityFilterOp()).withOp(new RelationOp()).withOp(new RelationFilterOp()).withOp(new EntityOp()).withOp(new EntityFilterOp()).typePattern();


        Pattern compileP2 = Pattern.compile(supportedPattern.get(1));

        Matcher matcher = compileP2.matcher(s1);
        Assert.assertFalse(matcher.matches());

        matcher = compileP2.matcher(s2);
        Assert.assertFalse(matcher.matches());

        matcher = compileP2.matcher(s3);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOne"),"EntityOp");
            Assert.assertEquals(matcher.group("relation"),"RelationOp");
            Assert.assertEquals(matcher.group("entityTwo"),"EntityOp");
        }

        matcher = compileP2.matcher(s4);
        if(matcher.matches()) {
            Assert.assertEquals(matcher.group("entityOne"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityOneFilter"),"EntityFilterOp:");
            Assert.assertEquals(matcher.group("relation"),"RelationOp");
            Assert.assertEquals(matcher.group("optionalRelFilter"),"RelationFilterOp:");
            Assert.assertEquals(matcher.group("entityTwo"),"EntityOp");
            Assert.assertEquals(matcher.group("optionalEntityTwoFilter"),"EntityFilterOp");
        }


    }

}
