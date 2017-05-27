package com.kayhut.fuse.stat.model.bucket;

/**
 * Created by benishue on 30-Apr-17.
 */
public class BucketRange<T> extends Bucket {

    //region Ctrs
    public BucketRange() {
        super();
    }

    public BucketRange(T start, T end) {
        super();
        this.start = start;
        this.end = end;
    }
    //endregion

    //region Getters & Setters
    public T getStart() {
        return start;
    }

    public void setStart(T start) {
        this.start = start;
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(T end) {
        this.end = end;
    }
    //endregion

    //region Fields
    private T start;
    private T end;
    //endregion

}
