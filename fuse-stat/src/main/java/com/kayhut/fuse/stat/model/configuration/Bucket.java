package com.kayhut.fuse.stat.model.configuration;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Bucket {

    //region Ctrs
    public Bucket() {
    }

    public Bucket(String start, String end) {
        this.start = start;
        this.end = end;
    }
    //endregion

    //region Getters & Setters
    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
    //endregion

    //region Fields
    private String start;
    private String end;
    //endregion
}
