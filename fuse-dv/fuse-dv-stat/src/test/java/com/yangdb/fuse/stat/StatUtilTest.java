package com.yangdb.fuse.stat;

import com.yangdb.fuse.stat.model.bucket.BucketRange;
import com.yangdb.fuse.stat.util.StatUtil;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class StatUtilTest {

    @Test
    public void testStringBucketsPrefix1(){
        int interval = 10;
        List<BucketRange<String>> bucketRanges = StatUtil.calculateAlphabeticBuckets(65, 58, 1, interval);
        Assert.assertEquals(bucketRanges.size(),6);
        for (int i = 0; i < bucketRanges.size()-1; i++) {
            Assert.assertEquals(bucketRanges.get(i).getStart(),Character.toString((char)(65 + i*interval)));
            Assert.assertEquals(bucketRanges.get(i).getEnd(),Character.toString((char)(65 + (i+1)*interval)));
        }

        Assert.assertEquals(bucketRanges.get(5).getStart(),Character.toString((char)(65 + 5*interval)));
        Assert.assertEquals(bucketRanges.get(5).getEnd(),Character.toString((char)(65 + 58)));
    }

    @Test
    public void testStringBucketsPrefix1_2(){
        int interval = 29;
        List<BucketRange<String>> bucketRanges = StatUtil.calculateAlphabeticBuckets(65, 58, 1, interval);
        Assert.assertEquals(bucketRanges.size(),2);
        for (int i = 0; i < bucketRanges.size()-1; i++) {
            Assert.assertEquals(bucketRanges.get(i).getStart(),Character.toString((char)(65 + i*interval)));
            Assert.assertEquals(bucketRanges.get(i).getEnd(),Character.toString((char)(65 + (i+1)*interval)));
        }

        Assert.assertEquals(bucketRanges.get(1).getStart(),Character.toString((char)(65 + 1*interval)));
        Assert.assertEquals(bucketRanges.get(1).getEnd(),Character.toString((char)(65 + 58)));
    }

    @Test
    public void testStringBucketsPrefix2(){
        int interval = 29;
        int prefix = 2;
        List<BucketRange<String>> bucketRanges = StatUtil.calculateAlphabeticBuckets(65, 58, prefix, interval);
        Assert.assertEquals(bucketRanges.size(),116);
        for (int i = 0; i < bucketRanges.size()-1; i++) {
            Assert.assertEquals(bucketRanges.get(i).getEnd(), bucketRanges.get(i+1).getStart());
        }

        Assert.assertEquals("AA", bucketRanges.get(0).getStart());
        Assert.assertEquals("{{",Stream.ofAll(bucketRanges).last().getEnd());
    }

    @Test
    public void testStringBucketsPrefix2_2(){
        int interval = 10;
        int prefix = 2;
        List<BucketRange<String>> bucketRanges = StatUtil.calculateAlphabeticBuckets(65, 58, prefix, interval);
        Assert.assertEquals(bucketRanges.size(),337);
        for (int i = 0; i < bucketRanges.size()-1; i++) {
            Assert.assertEquals(bucketRanges.get(i).getEnd(), bucketRanges.get(i+1).getStart());
        }

        Assert.assertEquals("AA", bucketRanges.get(0).getStart());
        Assert.assertEquals("{{",Stream.ofAll(bucketRanges).last().getEnd());
    }

    @Test
    public void testStringBucketsPrefix3(){
        int interval = 10;
        int prefix = 3;
        List<BucketRange<String>> bucketRanges = StatUtil.calculateAlphabeticBuckets(97, 26, prefix, interval);

        for (int i = 0; i < bucketRanges.size()-1; i++) {
            Assert.assertEquals(bucketRanges.get(i).getEnd(), bucketRanges.get(i+1).getStart());
        }
        Assert.assertEquals(bucketRanges.get(0).getStart(), "aaa");
        Assert.assertEquals(Stream.ofAll(bucketRanges).last().getEnd(), "{{{");
    }

}
