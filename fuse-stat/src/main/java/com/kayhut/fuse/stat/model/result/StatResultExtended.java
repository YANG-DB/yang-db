package com.kayhut.fuse.stat.model.result;

/**
 * Created by benishue on 03-May-17.
 */
public class StatResultExtended extends StatResult {

    // todo maybe for the future
    //region Ctors
    public StatResultExtended() {
    }

    public StatResultExtended(long count, double sum, double sumOfSquares, double avg, double min, double max, double variance, double stdDeviation) {
        this.count = count;
        this.sum = sum;
        this.sumOfSquares = sumOfSquares;
        this.avg = avg;
        this.min = min;
        this.max = max;
        this.variance = variance;
        this.stdDeviation = stdDeviation;
    }

    //endregion

    //region Getter & Setters

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getSumOfSquares() {
        return sumOfSquares;
    }

    public void setSumOfSquares(double sumOfSquares) {
        this.sumOfSquares = sumOfSquares;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public double getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(double stdDeviation) {
        this.stdDeviation = stdDeviation;
    }

    //endregion

    //region Fields
    private long count;
    private double sum;
    private double sumOfSquares;
    private double avg;
    private double min;
    private double max;
    private double variance;
    private double stdDeviation;
    //endregion

}
