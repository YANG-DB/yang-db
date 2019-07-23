package com.yangdb.fuse.epb.plan.statistics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
    public void testFindExistingValueLowerBound(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("a");
        Assert.assertTrue(aaaBucket.isPresent());
        Assert.assertEquals(aaaBucket.get().getLowerBound(), "a");
        Assert.assertEquals(aaaBucket.get().getHigherBound(), "b");
    }

    @Test
    public void testFindNonExistingValueUpperBound(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("b");
        Assert.assertFalse(aaaBucket.isPresent());
    }

    @Test
    public void testFindNonExistingValue(){
        Optional<Statistics.BucketInfo<String>> aaaBucket = histogram.findBucketContaining("ba");
        Assert.assertFalse(aaaBucket.isPresent());
    }

    @Test
    public void testFindAbove(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("aaa", true);
        Assert.assertEquals(2, bucketsAbove.size());
    }

    @Test
    public void testFindAbovePrecise(){
        List<Statistics.BucketInfo<String>> bucketsAbove = histogram.findBucketsAbove("b", true);
        Assert.assertEquals(1, bucketsAbove.size());
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


    @Test
    public void combineHistogramSimpleTest(){
        Statistics.HistogramStatistics<String> histogram1 = new Statistics.HistogramStatistics<>(
                        Arrays.asList(new Statistics.BucketInfo<>(100L, 10L, "a", "m"),
                                new Statistics.BucketInfo<>(50L, 15L, "m", "z")));
        Statistics.HistogramStatistics<String> mergedHistogram = Statistics.HistogramStatistics.combine(Arrays.asList(histogram1, histogram1));

        Assert.assertNotNull(mergedHistogram);
        Assert.assertEquals(2,mergedHistogram.getBuckets().size());
        Assert.assertEquals("a",mergedHistogram.getBuckets().get(0).getLowerBound());
        Assert.assertEquals("m",mergedHistogram.getBuckets().get(0).getHigherBound());
        Assert.assertEquals("m",mergedHistogram.getBuckets().get(1).getLowerBound());
        Assert.assertEquals("z",mergedHistogram.getBuckets().get(1).getHigherBound());
        Assert.assertEquals(200L,(long)mergedHistogram.getBuckets().get(0).getTotal());
        Assert.assertEquals(100L,(long)mergedHistogram.getBuckets().get(1).getTotal());
        Assert.assertEquals(10L,(long)mergedHistogram.getBuckets().get(0).getCardinality());
        Assert.assertEquals(15L,(long)mergedHistogram.getBuckets().get(1).getCardinality());
    }

    @Test
    public void combineHistogramSingleValueTest(){
        Statistics.HistogramStatistics<String> histogram1 = new Statistics.HistogramStatistics<>(
                Arrays.asList(new Statistics.BucketInfo<>(100L, 1L, "a", "a"),
                        new Statistics.BucketInfo<>(50L, 1L, "b", "b")));
        Statistics.HistogramStatistics<String> mergedHistogram = Statistics.HistogramStatistics.combine(Arrays.asList(histogram1, histogram1));

        Assert.assertNotNull(mergedHistogram);
        Assert.assertEquals(2,mergedHistogram.getBuckets().size());
        Assert.assertEquals("a",mergedHistogram.getBuckets().get(0).getLowerBound());
        Assert.assertEquals("a",mergedHistogram.getBuckets().get(0).getHigherBound());
        Assert.assertEquals("b",mergedHistogram.getBuckets().get(1).getLowerBound());
        Assert.assertEquals("b",mergedHistogram.getBuckets().get(1).getHigherBound());
        Assert.assertEquals(200L,(long)mergedHistogram.getBuckets().get(0).getTotal());
        Assert.assertEquals(100L,(long)mergedHistogram.getBuckets().get(1).getTotal());
        Assert.assertEquals(1L,(long)mergedHistogram.getBuckets().get(0).getCardinality());
        Assert.assertEquals(1L,(long)mergedHistogram.getBuckets().get(1).getCardinality());
    }

    @Test
    public void combineHistogramComplexTest(){
        Statistics.HistogramStatistics<String> histogram1 = new Statistics.HistogramStatistics<>(
                Arrays.asList(new Statistics.BucketInfo<>(100L, 1L, "a", "a"),
                        new Statistics.BucketInfo<>(50L, 10L, "b", "e"),
                        new Statistics.BucketInfo<>(50L, 10L, "e", "f"),
                        new Statistics.BucketInfo<>(100L, 1L, "g", "g")));
        Statistics.HistogramStatistics<String> histogram2 = new Statistics.HistogramStatistics<>(
                Arrays.asList(
                        new Statistics.BucketInfo<>(50L, 10L, "b", "e"),
                        new Statistics.BucketInfo<>(100L, 1L, "g", "g"),
                        new Statistics.BucketInfo<>(100L, 20L, "h", "i")));
        Statistics.HistogramStatistics<String> mergedHistogram = Statistics.HistogramStatistics.combine(Arrays.asList(histogram1, histogram2));

        Assert.assertNotNull(mergedHistogram);
        Assert.assertEquals(5,mergedHistogram.getBuckets().size());
        Assert.assertEquals("a",mergedHistogram.getBuckets().get(0).getLowerBound());
        Assert.assertEquals("a",mergedHistogram.getBuckets().get(0).getHigherBound());
        Assert.assertEquals("b",mergedHistogram.getBuckets().get(1).getLowerBound());
        Assert.assertEquals("e",mergedHistogram.getBuckets().get(1).getHigherBound());
        Assert.assertEquals("e",mergedHistogram.getBuckets().get(2).getLowerBound());
        Assert.assertEquals("f",mergedHistogram.getBuckets().get(2).getHigherBound());
        Assert.assertEquals("g",mergedHistogram.getBuckets().get(3).getLowerBound());
        Assert.assertEquals("g",mergedHistogram.getBuckets().get(3).getHigherBound());
        Assert.assertEquals("h",mergedHistogram.getBuckets().get(4).getLowerBound());
        Assert.assertEquals("i",mergedHistogram.getBuckets().get(4).getHigherBound());
        Assert.assertEquals(100L,(long)mergedHistogram.getBuckets().get(0).getTotal());
        Assert.assertEquals(100L,(long)mergedHistogram.getBuckets().get(1).getTotal());
        Assert.assertEquals(50L,(long)mergedHistogram.getBuckets().get(2).getTotal());
        Assert.assertEquals(200L,(long)mergedHistogram.getBuckets().get(3).getTotal());
        Assert.assertEquals(100L,(long)mergedHistogram.getBuckets().get(4).getTotal());
        Assert.assertEquals(1L,(long)mergedHistogram.getBuckets().get(0).getCardinality());
        Assert.assertEquals(10L,(long)mergedHistogram.getBuckets().get(1).getCardinality());
        Assert.assertEquals(10L,(long)mergedHistogram.getBuckets().get(2).getCardinality());
        Assert.assertEquals(1L,(long)mergedHistogram.getBuckets().get(3).getCardinality());
        Assert.assertEquals(20L,(long)mergedHistogram.getBuckets().get(4).getCardinality());
    }

}
