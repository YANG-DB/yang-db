package org.kayhut.fuse.model.query;

/**
 * Created by User on 16/02/2017.
 */
public class Start extends ElementBase {

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
