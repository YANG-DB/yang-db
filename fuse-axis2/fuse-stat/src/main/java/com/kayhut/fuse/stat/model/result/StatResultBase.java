package com.kayhut.fuse.stat.model.result;

import com.kayhut.fuse.stat.model.enums.DataType;

public abstract class StatResultBase {

    //region Ctr
    public StatResultBase() {
    }

    public StatResultBase(String index, String type, String field, String key, DataType dataType, long docCount, long cardinality) {
        this.index = index;
        this.type = type;
        this.field = field;
        this.key = key;
        this.dataType = dataType;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    //endregion

    //region Fields
    private String index;
    private String type;
    private String field;
    private String key;
    private DataType dataType;
    private long docCount;
    private long cardinality;
    //endregion
}
