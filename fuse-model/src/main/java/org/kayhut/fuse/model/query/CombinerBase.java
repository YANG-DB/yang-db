package org.kayhut.fuse.model.query;

/**
 * Created by benishue on 17/02/2017.
 */
public class CombinerBase extends ElementBase {
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
