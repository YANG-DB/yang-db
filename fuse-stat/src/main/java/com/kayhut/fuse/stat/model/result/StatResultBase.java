package com.kayhut.fuse.stat.model.result;

public abstract class StatResult {

    //region Ctr
    public StatResult() {
    }

    public StatResult(String index, String type, String field, long docCount, long cardinality) {
        this.index = index;
        this.type = type;
        this.field = field;
        this.docCount = docCount;
        this.cardinality = cardinality;
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
    private long docCount;
    private long cardinality;
    //endregion
}
