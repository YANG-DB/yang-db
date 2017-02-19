package org.kayhut.fuse.model.query;

/**
 * Created by user on 16-Feb-17.
 */
public class TypedEntity extends ElementBase {

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

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

    public int getBelow() {
        return below;
    }

    public void setBelow(int below) {
        this.below = below;
    }

    //region Fields
    private	String eTag;
    private int	eType;
    private	int next;
    private	int below;
    //endregion


}
