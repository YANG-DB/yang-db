package com.kayhut.fuse.stat.model.result;

/**
 * Created by benishue on 03-May-17.
 */
public class NumericStatResult extends StatResult {

    //region Ctors
    public NumericStatResult() {
    }

    public NumericStatResult(String index, String type, String field, double lowerBound, double upperBound, long count, long cardinality) {
        super(index,type,field,count,cardinality);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    //endregion

    //region Getter & Setters
    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }
    //endregion

    //region Fields
    private double lowerBound;
    private double upperBound;
    //endregion
}
