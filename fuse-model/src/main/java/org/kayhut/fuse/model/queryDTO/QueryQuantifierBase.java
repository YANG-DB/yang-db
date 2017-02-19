package org.kayhut.fuse.model.queryDTO;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class QueryQuantifierBase extends  QueryElementBase {

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
