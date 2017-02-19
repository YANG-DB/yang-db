package com.kayhut.fuse.model.query;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class Quant1 extends QuantBase {
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    //region Fields
    private int b;
    private List<Integer> next;
    //endregion

}
