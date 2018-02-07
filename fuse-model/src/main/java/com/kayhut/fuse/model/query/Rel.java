package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.entity.Typed;

import java.util.Collections;
import java.util.List;

/**
 * Created by user on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Rel extends EBase implements Next<Integer>, Below<Integer> ,Typed.rTyped{
    public enum Direction {
        R,
        L,
        RL;

        public static Direction reverse(Direction dir) {
            Direction reversed = Direction.RL;
            switch (dir) {
                case R:
                    reversed = Direction.L;
                    break;
                case L:
                    reversed = Direction.R;
                    break;
            }
            return reversed;
        }

        public String translatedName(){
            String name = "both";
            switch(this){
                case R:
                    name = "out";
                    break;
                case L:
                    name = "in";
                    break;
            }
            return name;
        }
    }

    //region Constructors
    public Rel() {
        this.reportProps = Collections.emptyList();
    }

    public Rel(int eNum, String rType, Direction dir, String wrapper, int next, int b) {
        this(eNum, rType, dir, wrapper, Collections.emptyList(), next, b);
    }

    public Rel(int eNum, String rType, Direction dir, String wrapper, List<String> reportProps, int next, int b) {
        super(eNum);
        this.rType = rType;
        this.dir = dir;
        this.wrapper = wrapper;
        this.reportProps = reportProps;
        this.next = next;
        this.b = b;
    }
    //endregion

    //region Properties
    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
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

    public List<String> getReportProps() {
        return reportProps;
    }

    public void setReportProps(List<String> reportProps) {
        this.reportProps = reportProps;
    }
    //endregion

    //region Override Methods
    @Override
    public Rel clone() {
        return new Rel(geteNum(),getrType(),getDir(),getWrapper(),getNext(),getB());
    }
    //endregion

    //region Fields
    private String rType;
    private Direction dir;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String wrapper;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;

    private List<String> reportProps;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Rel rel = (Rel) o;

        if (!rType.equals(rel.rType)) return false;
        if (next != rel.next) return false;
        if (b != rel.b) return false;
        if (dir != rel.dir) return false;
        return wrapper != null ? wrapper.equals(rel.wrapper) : rel.wrapper == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rType.hashCode();
        result = 31 * result + dir.hashCode();
        result = 31 * result + (wrapper != null ? wrapper.hashCode() : 0);
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
}
