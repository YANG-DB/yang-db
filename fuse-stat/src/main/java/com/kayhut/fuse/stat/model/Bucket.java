package com.kayhut.fuse.stat.model;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Bucket {

    public Bucket() {
    }

    public Bucket(String start, String end) {
        this.start = start;
        this.end = end;
    }

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

    //region Fields
    private String start;
    private String end;
    //endregion
}
