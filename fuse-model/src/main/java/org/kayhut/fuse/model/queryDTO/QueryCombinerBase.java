package org.kayhut.fuse.model.queryDTO;

/**
 * Created by benishue on 17/02/2017.
 */
public class QueryCombinerBase extends QueryElementBase {
    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    //region Fields
    private int next;
    //endregion

}
