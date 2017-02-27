package com.kayhut.fuse.model.query;


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

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    private List<Integer> vTypes;
    private	List<Integer> nvTypes;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int	next;

}
