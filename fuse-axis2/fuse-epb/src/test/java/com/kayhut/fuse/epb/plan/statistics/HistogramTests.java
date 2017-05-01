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
        buckets.add(new Statistics.BucketInfo<>(1L,1L,"ba", "z"));
    }

    @Test
    public void testFindExistingValue(){
        Optional<Statistics.BucketInfo<String>> aaa = histogram.findBucketContaining("aaa");
        Assert.assertTrue(aaa.isPresent());
    }


}
