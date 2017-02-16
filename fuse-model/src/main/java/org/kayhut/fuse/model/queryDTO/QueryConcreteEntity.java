package org.kayhut.fuse.model.queryDTO;

/**
 * Created by user on 16-Feb-17.
 */
public class QueryConcreteEntity extends QueryElementBase {

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public int geteId() {
        return eId;
    }

    public void seteId(int eId) {
        this.eId = eId;
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

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    //region Fields
    private String eTag;
    private int eId;
    private int eType;
    private String eName;
    private int next;
    //endregion


}
