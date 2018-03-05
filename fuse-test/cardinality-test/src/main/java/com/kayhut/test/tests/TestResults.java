package com.kayhut.test.tests;

import com.google.common.math.Stats;

import java.util.List;

public class TestResults {
    public Stats getErrorStats() {
        return errorStats;
    }

    public void setErrorStats(Stats errorStats) {
        this.errorStats = errorStats;
    }

    public Double getAverageBucketSize() {
        return averageBucketSize;
    }

    public void setAverageBucketSize(Double averageBucketSize) {
        this.averageBucketSize = averageBucketSize;
    }

    public Stats getAbsoluteErrorStats() {
        return absoluteErrorStats;
    }

    public void setAbsoluteErrorStats(Stats absoluteErrorStats) {
        this.absoluteErrorStats = absoluteErrorStats;
    }

    public Stats getZeroAbsoluteErrorStats() {
        return zeroAbsoluteErrorStats;
    }

    public void setZeroAbsoluteErrorStats(Stats zeroAbsoluteErrorStats) {
        this.zeroAbsoluteErrorStats = zeroAbsoluteErrorStats;
    }

    public Stats getIntersectionSizesStats() {
        return intersectionSizesStats;
    }

    public void setIntersectionSizesStats(Stats intersectionSizesStats) {
        this.intersectionSizesStats = intersectionSizesStats;
    }

    private Stats errorStats;
    private Stats absoluteErrorStats;
    private Stats zeroAbsoluteErrorStats;
    private Double averageBucketSize;
    private Stats intersectionSizesStats;
}
