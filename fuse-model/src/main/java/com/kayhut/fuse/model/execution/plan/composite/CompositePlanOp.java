package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.execution.plan.PlanOp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

import static com.kayhut.fuse.model.Utils.fullPattern;

/**
 * Created by Roman on 24/04/2017.
 */
public abstract class CompositePlanOp extends PlanOp implements Cloneable {
    //region Empty
    public static class Empty extends CompositePlanOp {

        //region CompositePlanOp Implementation
        @Override
        public List<PlanOp> getOps() {
            return Collections.emptyList();
        }
        //endregion
    }

    public static Empty empty() {
        return empty;
    }

    private static Empty empty = new Empty();
    //endregion

    //region Constructors
    private CompositePlanOp() {}

    public CompositePlanOp(Iterable<PlanOp> ops) {
        this.ops = Stream.ofAll(ops).toJavaList();
    }

    public CompositePlanOp(PlanOp...ops) {
        this(Stream.of(ops));
    }

    public CompositePlanOp(CompositePlanOp compositePlanOp) {
        this(compositePlanOp.getOps());
    }
    //endregion

    //region Public Methods
    public <T extends CompositePlanOp> T  withOp(PlanOp op) {
        try {
            CompositePlanOp clone = (CompositePlanOp)clone();
            clone.getOps().add(op);
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends CompositePlanOp> T  append(CompositePlanOp compositePlanOp) {
        try {
            CompositePlanOp clone = (CompositePlanOp)clone();
            clone.getOps().addAll(compositePlanOp.getOps());
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends CompositePlanOp> T from(PlanOp fromOp) {
        return fromTo(this.getOps().indexOf(fromOp), this.getOps().size());
    }

    public <T extends CompositePlanOp> T to(PlanOp toOp) {
        return fromTo(0, this.getOps().indexOf(toOp));
    }

    public <T extends CompositePlanOp> T fromTo(PlanOp fromOp, PlanOp toOp) {
        return fromTo(this.getOps().indexOf(fromOp), this.getOps().indexOf(toOp));
    }

    public <T extends CompositePlanOp> T fromTo(int indexFrom, int indexTo) {
        if (indexFrom < 0 || indexTo < 0) {
            return (T)empty();
        }

        try {
            CompositePlanOp clone = (CompositePlanOp)super.clone();
            clone.ops = Stream.ofAll(this.getOps()).drop(indexFrom).take(indexTo - indexFrom).toJavaList();
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    //endregion

    //region Properties
    public List<PlanOp> getOps() {
        return this.ops;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + fullPattern(this.getOps()) + "]";
    }

    public Object clone()throws CloneNotSupportedException{
        CompositePlanOp clone = (CompositePlanOp)super.clone();
        clone.ops = Stream.ofAll(this.getOps()).toJavaList();
        return clone;
    }
    //endregion

    //region Fields
    private List<PlanOp> ops;
    //endregion
}
