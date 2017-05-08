package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;
import com.kayhut.fuse.model.Next;

/**
 * Created by user on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Rel extends EBase implements Next<Integer>, Below<Integer> {
    public enum Direction {
        R,
        L,
        RL;


    }

    public int getrType() {
        return rType;
    }

    public void setrType(int rType) {
        this.rType = rType;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

   //region Fields
    private int rType;
    private Direction dir;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String wrapper;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Rel rel = (Rel) o;

        if (rType != rel.rType) return false;
        if (next != rel.next) return false;
        if (b != rel.b) return false;
        if (dir != rel.dir) return false;
        return wrapper != null ? wrapper.equals(rel.wrapper) : rel.wrapper == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rType;
        result = 31 * result + dir.hashCode();
        result = 31 * result + (wrapper != null ? wrapper.hashCode() : 0);
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
}
