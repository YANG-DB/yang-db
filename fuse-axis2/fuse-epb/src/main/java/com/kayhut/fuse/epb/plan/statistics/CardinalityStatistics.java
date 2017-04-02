package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 31/03/2017.
 */
public class CardinalityStatistics {
    private long cardinality;
    private long total;

    public CardinalityStatistics(long cardinality, long total) {
        this.cardinality = cardinality;
        this.total = total;
    }

    public long getCardinality() {
        return cardinality;
    }

    public long getTotal() {
        return total;
    }
}
