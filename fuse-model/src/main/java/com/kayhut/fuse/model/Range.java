package com.kayhut.fuse.model;


public class Range {
    //region Constructors
    public Range() {

    }

    public Range(long lower, long upper) {
        this.lower = lower;
        this.upper = upper;
    }
    //endregion

    //region Properties
    public long getUpper() {
        return upper;
    }

    public void setUpper(long upper) {
        this.upper = upper;
    }

    public long getLower() {
        return lower;
    }

    public void setLower(long lower) {
        this.lower = lower;
    }
    //endregion

    //region Fields
    private long upper;
    private long lower;
    //endregion
}