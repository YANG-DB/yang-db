package com.kayhut.fuse.stat.model.result;

/**
 * Created by benishue on 03-May-17.
 */
public class StringStatResult extends StatResult {

    //region Ctors
    public StringStatResult() {
    }

    public StringStatResult(String index, String type, String field, String key, String lowerBound, String upperBound, long count, long cardinality) {
        super(index,type,field,count,cardinality);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    //endregion

    //region Getter & Setters

    public String getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }
    //endregion

    //region Fields
    private String lowerBound;
    private String upperBound;
    //endregion

}
