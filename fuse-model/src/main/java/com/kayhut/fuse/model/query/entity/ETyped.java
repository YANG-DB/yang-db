package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;

/**
 * Created by user on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase implements Typed , Below<Integer> {
    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
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

    //region Fields
    private int	eType;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int b;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ETyped eTyped = (ETyped) o;

        if (eType != eTyped.eType) return false;
        if (next != eTyped.next) return false;
        return b == eTyped.b;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eType;
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
}
