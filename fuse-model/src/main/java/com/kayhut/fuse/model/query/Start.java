package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Next;

/**
 * Created by User on 16/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Start extends EBase implements Next<Integer> {

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    //region Fields
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Start start = (Start) o;

        if (next != start.next) return false;
        return b == start.b;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
}
