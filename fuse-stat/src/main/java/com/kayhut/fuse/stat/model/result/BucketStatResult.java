package com.kayhut.fuse.stat.model.result;

/**
 * Created by benishue on 03-May-17.
 */
public class BucketStatResult extends ExtendedStatResult {

    //region Ctors
    public BucketStatResult() {
    }

    public BucketStatResult(String index, String type, String field, String key, String lowerBound, String upperBound,  long count, long cardinality) {
        this.index = index;
        this.type = type;
        this.field = field;
        this.key = key;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.cardinality = cardinality;
        this.docCount = count;
    }

    public BucketStatResult(String index, String type, String field, String key, String lowerBound, String upperBound, long count, double sum, double sumOfSquares, double avg, double min, double max, double variance, double stdDeviation, long cardinality) {
        super(count,sum,sumOfSquares,avg,min,max,variance,stdDeviation);
        this.index = index;
        this.type = type;
        this.field = field;
        this.key = key;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.cardinality = cardinality;
        this.docCount = count;
    }

    //endregion

    //region Getter & Setters

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public long getDocCount() {
        return docCount;
    }

    public void setDocCount(long docCount) {
        this.docCount = docCount;
    }

    public long getCardinality() {
        return cardinality;
    }

    public void setCardinality(long cardinality) {
        this.cardinality = cardinality;
    }

    //endregion

    //region Fields
    private String index;
    private String type;
    private String field;
    private String key;
    private String lowerBound;
    private String upperBound;
    private long docCount;
    private long cardinality;
    //endregion
}
