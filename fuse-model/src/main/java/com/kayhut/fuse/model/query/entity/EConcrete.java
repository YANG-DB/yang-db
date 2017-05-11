package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EConcrete extends EEntityBase implements Typed{
    public String geteID() {
        return eID;
    }

    public void seteID(String eID) {
        this.eID = eID;
    }

    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    //region Fields
    private String eID;
    private int eType;
    private String eName;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EConcrete eConcrete = (EConcrete) o;

        if (eType != eConcrete.eType) return false;
        if (next != eConcrete.next) return false;
        if (!eID.equals(eConcrete.eID)) return false;
        return eName.equals(eConcrete.eName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eID.hashCode();
        result = 31 * result + eType;
        result = 31 * result + eName.hashCode();
        result = 31 * result + next;
        return result;
    }
}
