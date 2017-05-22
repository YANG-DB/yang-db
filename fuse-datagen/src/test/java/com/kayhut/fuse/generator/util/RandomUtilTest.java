package com.kayhut.fuse.generator.util;

import com.google.common.primitives.Doubles;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by benishue on 22-May-17.
 */
public class RandomUtilTest {


    @Test
    public void expDistTest() throws Exception {
        double[] expDistArray = RandomUtil.getExpDistArray(100, 1.0, 0.5);

        List<Double> list = Arrays.stream(expDistArray).boxed().collect(Collectors.toList());
        double sum = list.stream().mapToDouble(d -> d).sum();
        System.out.println(sum);
        assertTrue(1.0 == Math.floor(sum));
    }

    @Test
    public void getCumulativeDistArray() throws Exception {
        double[] expDistArray = RandomUtil.getExpDistArray(100, 1.0, 0.5);
        double[] cumulativeDistArray = RandomUtil.getCumulativeDistArray(expDistArray);
        assertTrue(Math.round(cumulativeDistArray[cumulativeDistArray.length - 1]) == 1.0);
    }
}