package org.kayhut.fuse.model.query;

import java.util.List;

/**
 * Created by user on 16-Feb-17.
 */
public class UntypedEntity extends ElementBase {

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

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

    private	String eTag;
    private List<Integer> vTypes;
    private	List<Integer> nvTypes;
    private	int	next;

}
