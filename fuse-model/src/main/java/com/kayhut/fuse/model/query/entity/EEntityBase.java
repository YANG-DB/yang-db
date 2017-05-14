package com.kayhut.fuse.model.query.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 27/02/2017.
 */
public abstract class EEntityBase extends EBase implements Next<Integer>, Below<Integer> {
    //region Constructors
    public EEntityBase() {}

    public EEntityBase(int eNum, String eTag, int next, int b) {
        super(eNum);
        this.eTag = eTag;
        this.next = next;
        this.b = b;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EEntityBase that = (EEntityBase) o;

        if (!eTag.equals(that.eTag)) return false;
        if (next != that.next) return false;
        return b == that.b;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eTag.hashCode();
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
    //endregion

    //region Properties
    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private	String eTag;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int b;
    //endregion
}
