package org.kayhut.fuse.model.queryDTO;

/**
 * Created by User on 16/02/2017.
 */
public class QueryStart extends QueryElementBase  {

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
