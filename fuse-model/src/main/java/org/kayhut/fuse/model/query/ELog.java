package org.kayhut.fuse.model.query;

/**
 * Created by user on 16-Feb-17.
 */
public class ELog extends EBase {

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

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
    private	String eTag;
    private	String fName;
    private	String eName;
    private	int	next;
    //endregion
}
