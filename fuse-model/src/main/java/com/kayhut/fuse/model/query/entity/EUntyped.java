package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by user on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EUntyped extends EEntityBase {
    public List<Integer> getvTypes() {
        return vTypes;
    }

    public void setvTypes(List<Integer> vTypes) {
        this.vTypes = vTypes;
    }

    public List<Integer> getNvTypes() {
        return nvTypes;
    }

    public void setNvTypes(List<Integer> nvTypes) {
        this.nvTypes = nvTypes;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    //region Fields
    private List<Integer> vTypes;
    private	List<Integer> nvTypes;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int	next;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EUntyped eUntyped = (EUntyped) o;

        if (next != eUntyped.next) return false;
        if (vTypes != null ? !vTypes.equals(eUntyped.vTypes) : eUntyped.vTypes != null) return false;
        return nvTypes != null ? nvTypes.equals(eUntyped.nvTypes) : eUntyped.nvTypes == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (vTypes != null ? vTypes.hashCode() : 0);
        result = 31 * result + (nvTypes != null ? nvTypes.hashCode() : 0);
        result = 31 * result + next;
        return result;
    }
}
