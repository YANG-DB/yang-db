package com.kayhut.fuse.epb.plan.statistics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 01/05/2017.
 */
public class HistogramSingleValueBucketTests {
    Statistics.HistogramStatistics<String> histogram;

    @Before
    public void setup(){
        List<Statistics.BucketInfo<String>> buckets = new ArrayList<>();
        histogram = new Statistics.HistogramStatistics<>(buckets);

        buckets.add(new Statistics.BucketInfo<>(10L, 1L, "a", "a" ));
        buckets.add(new Statistics.BucketInfo<>(10L,1L,"b", "b"));
    }

    @Test
    public void testFindExistingValue(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("a");
        Assert.assertTrue(aaaBucket.isPresent());
        Assert.assertEquals(aaaBucket.get().getLowerBound(), "a");
        Assert.assertEquals(aaaBucket.get().getHigherBound(), "a");
    }

    @Test
    public void testFindExistingValue2(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("b");
        Assert.assertTrue(aaaBucket.isPresent());
        Assert.assertEquals(aaaBucket.get().getLowerBound(), "b");
        Assert.assertEquals(aaaBucket.get().getHigherBound(), "b");
    }

    @Test
    public void testFindNonExistingValue(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("ba");
        Assert.assertFalse(aaaBucket.isPresent());
    }

    @Test
    public void testFindNonExistingValue2(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("0");
        Assert.assertFalse(aaaBucket.isPresent());
    }

    @Test
    public void testFindAbove(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("aaa",true);
        Assert.assertEquals(1, bucketsAbove.size());
    }

    @Test
    public void testFindAbovePrecise(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("b", true);
        Assert.assertEquals(1, bucketsAbove.size());
    }

    @Test
    public void testFindAboveNonInclusive2(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("b", false);
        Assert.assertEquals(0, bucketsAbove.size());
    }

    @Test
    public void testFindBelow(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsBelow("ccc", true);
        Assert.assertEquals(2, bucketsAbove.size());
    }

    @Test
    public void testFindBelowPrecise(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsBelow("bb", true);
        Assert.assertEquals(2, bucketsAbove.size());
    }



}
