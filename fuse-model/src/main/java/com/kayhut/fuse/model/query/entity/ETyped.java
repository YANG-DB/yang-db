package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase {
    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    //region Fields
    private int	eType;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int b;
    //endregion


}
