package org.kayhut.fuse.model.queryDTO;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class QueryQuantifier1 extends QueryQuantifierBase {
    public int getBelow() {
        return below;
    }

    public void setBelow(int below) {
        this.below = below;
    }

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    //region Fields
    private int below;
    private List<Integer> next;
    //endregion

}
