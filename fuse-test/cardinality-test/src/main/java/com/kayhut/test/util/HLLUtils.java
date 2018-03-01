package com.kayhut.test.util;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.clearspring.analytics.stream.cardinality.ICardinality;

public class HLLUtils {
    public static long intersect(HyperLogLogPlus hll1, HyperLogLogPlus hll2) throws CardinalityMergeException {
        HyperLogLogPlus merge = (HyperLogLogPlus) hll1.merge(hll2);
        return hll1.cardinality() + hll2.cardinality() - merge.cardinality();
    }
}
