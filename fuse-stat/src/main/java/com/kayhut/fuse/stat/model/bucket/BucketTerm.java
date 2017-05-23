package com.kayhut.fuse.stat.model.bucket;

/**
 * Created by benishue on 22-May-17.
 */
public class BucketTerm<T> extends Bucket {

    public BucketTerm() {
        super();
    }

    public BucketTerm(T term) {
        super();
        this.term = term;
    }

    public T getTerm() {
        return term;
    }

    public void setTerm(T term) {
        this.term = term;
    }

    private T term;
}
