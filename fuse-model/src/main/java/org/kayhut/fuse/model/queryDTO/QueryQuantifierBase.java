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

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    //region Fields
    private String qType;
    private int branches;
    private List<Integer> next;
    //endregion

}
