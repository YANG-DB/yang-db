package com.kayhut.fuse.stat.model.configuration.bucket;

/**
 * Created by benishue on 22-May-17.
 */
public class BucketTerm extends Bucket {

    public BucketTerm() {
        super();
    }

    public BucketTerm(String term) {
        super();
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    private String term;
}
