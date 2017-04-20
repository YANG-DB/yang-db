package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.model.execution.plan.costs.CostCalculator;

/**
 * Created by moti on 4/2/2017.
 */
public class CostCalculationUtil {
    public static <T extends Comparable<T>> CostCalculator.Cost calculateTermsCost(Statistics.HistogramStatistics<T> histogramStatistics, T[] terms){
        double total = 0.0;

        // TODO: binary search on histogram
        for (Statistics.BucketInfo<T> bucketInfo : histogramStatistics.getBuckets()) {
            for (T term : terms) {
                if (bucketInfo.isValueInRange(term)) {
                    if(bucketInfo.getCardinality() > 0){
                        total += bucketInfo.getTotal() / bucketInfo.getCardinality();
                    }
                }
            }
        }

        return new CostCalculator.Cost(total,0,0);
    }

    public static CostCalculator.Cost calculateCostForCardinality(Statistics.CardinalityStatistics cardinalityStatistics){
        return new CostCalculator.Cost(cardinalityStatistics.getTotal() / (double)cardinalityStatistics.getCardinality(),0,0);
    }
}
