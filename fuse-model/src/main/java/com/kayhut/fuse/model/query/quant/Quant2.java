package com.kayhut.fuse.model.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Next;

import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quant2 extends QuantBase {

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }

    //region Fields
    private List<Integer> next = Collections.emptyList();
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Quant2 quant2 = (Quant2) o;

        return next.equals(quant2.next);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + next.hashCode();
        return result;
    }
}
