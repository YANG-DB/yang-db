package com.kayhut.fuse.model.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Next;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quant1 extends QuantBase implements Next<List<Integer>> {
    //region Constructors
    public Quant1() {
        super();
    }

    public Quant1(int eNum, QuantType qType, Iterable<Integer> next, int b) {
        super(eNum, qType);
        this.next = Stream.ofAll(next).toJavaList();
        this.b = b;
    }
    //endregion

    //region Properties
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
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Quant1 quant1 = (Quant1) o;

        if (b != quant1.b) return false;
        return next.equals(quant1.next);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + b;
        result = 31 * result + next.hashCode();
        return result;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    private List<Integer> next;
    //endregion
}
