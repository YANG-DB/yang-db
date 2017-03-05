package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ELog extends EEntityBase {
    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    //region Fields
    private	String fName;
    private	String eName;
    private	int	next;
    //endregion
}
