package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.statistics.BucketInfo;
import com.kayhut.fuse.epb.plan.statistics.CardinalityStatistics;
import com.kayhut.fuse.epb.plan.statistics.HistogramStatistics;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;

/**
 * Created by moti on 4/2/2017.
 */
public class CostCalculationUtil {
    public static <T extends Comparable<T>> SingleCost calculateTermsCost(HistogramStatistics<T> histogramStatistics, T[] terms){
        double total = 0.0;

        // TODO: binary search on histogram
        for (BucketInfo<T> bucketInfo : histogramStatistics.getBuckets()) {
            for (T term : terms) {
                if (bucketInfo.isValueInRange(term)) {
                    if(bucketInfo.getCardinality() > 0){
                        total += bucketInfo.getTotal() / bucketInfo.getCardinality();
                    }
                }
            }
        }

        return new SingleCost(total);
    }

    public static SingleCost calculateCostForCardinality(CardinalityStatistics cardinalityStatistics){
        return new SingleCost(cardinalityStatistics.getTotal() / (double)cardinalityStatistics.getCardinality());
    }
}
