package org.kayhut.fuse.model.query;

/**
 * Created by benishue on 17/02/2017.
 */
public class QuantifierBase extends ElementBase {

    public String getqType() {
        return qType;
    }

    public void setqType(String qType) {
        this.qType = qType;
    }

    public int getBranches() {
        return branches;
    }

    public void setBranches(int branches) {
        this.branches = branches;
    }

    //region Fields
    private String qType;
    private int branches;
    //endregion

}
