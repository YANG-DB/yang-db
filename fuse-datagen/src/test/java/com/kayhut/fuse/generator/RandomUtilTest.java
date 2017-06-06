package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.util.RandomUtil;
import org.junit.Test;

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
        //System.out.println(sum);
        assertEquals(1.0, sum, 0.1);
    }

    @Test
    public void getCumulativeDistArray() throws Exception {
        double[] expDistArray = RandomUtil.getExpDistArray(100, 1.0, 0.5);
        double[] cumulativeDistArray = RandomUtil.getCumulativeDistArray(expDistArray);
        //Since we are talking on statistics the number supposed to be close to 1.0
        assertEquals(cumulativeDistArray[cumulativeDistArray.length - 1], 1.0, 0.1 );
    }
}