package org.kayhut.fuse.model.queryDTO;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class QueryQuantifier2 extends QueryQuantifierBase {

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    //region Fields
    private List<Integer> next;
    //endregion


}
