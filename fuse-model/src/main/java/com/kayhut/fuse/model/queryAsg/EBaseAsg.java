package com.kayhut.fuse.model.queryAsg;

import com.kayhut.fuse.model.query.EBase;

import java.util.List;

/**
 * Created by benishue on 23-Feb-17.
 */
public class EBaseAsg{

    public List<EBaseAsg> getNext() {
        return next;
    }

    public void setNext(List<EBaseAsg> next) {
        this.next = next;
    }

    public List<EBaseAsg> getB() {
        return b;
    }

    public void setB(List<EBaseAsg> b) {
        this.b = b;
    }

    public EBase geteBase() {
        return eBase;
    }

    public void seteBase(EBase eBase) {
        this.eBase = eBase;
    }

    public List<EBaseAsg> getParents() {
        return parents;
    }

    public void setParents(List<EBaseAsg> parents) {
        this.parents = parents;
    }

    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }

    //region Fields
    private int eNum;
    private EBase eBase;
    private List<EBaseAsg> next;
    private List<EBaseAsg> b;
    private List<EBaseAsg> parents;
    //endregion
}
