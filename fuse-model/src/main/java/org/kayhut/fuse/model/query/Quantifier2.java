package org.kayhut.fuse.model.query;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class Quantifier2 extends QuantifierBase {

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
