package com.kayhut.fuse.epb.plan.statistics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 01/05/2017.
 */
public class HistogramTests {
    Statistics.HistogramStatistics<String> histogram;

    @Before
    public void setup(){
        List<Statistics.BucketInfo<String>> buckets = new ArrayList<>();
        histogram = new Statistics.HistogramStatistics<>(buckets);

        buckets.add(new Statistics.BucketInfo<>(1L, 1L, "a", "b" ));
        buckets.add(new Statistics.BucketInfo<>(1L,1L,"bb", "z"));
    }

    @Test
    public void testFindExistingValue(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("aaa");
        Assert.assertTrue(aaaBucket.isPresent());
        Assert.assertEquals(aaaBucket.get().getLowerBound(), "a");
        Assert.assertEquals(aaaBucket.get().getHigherBound(), "b");
    }

    @Test
    public void testFindAbove(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("aaa", true);
        Assert.assertEquals(2, bucketsAbove.size());
    }

    @Test
    public void testFindAbovePrecise(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("b", true);
        Assert.assertEquals(2, bucketsAbove.size());
    }

    @Test
    public void testFindAboveNonInclusive(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("ba", false);
        Assert.assertEquals(1, bucketsAbove.size());
    }

    @Test
    public void testFindAboveNonInclusive2(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("b", false);
        Assert.assertEquals(1, bucketsAbove.size());
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

    @Test
    public void testFindBelowNonInclusive(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("ba", false);
        Assert.assertEquals(1, bucketsAbove.size());
    }

    @Test
    public void testFindBelowNonInclusive2(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("bb", false);
        Assert.assertEquals(1, bucketsAbove.size());
    }
}
