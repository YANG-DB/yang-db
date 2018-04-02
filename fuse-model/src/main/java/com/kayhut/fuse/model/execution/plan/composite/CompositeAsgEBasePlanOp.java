package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;

/**
 * Created by Roman on 11/25/2017.
 */
public class CompositeAsgEBasePlanOp<T extends EBase> extends CompositePlanOp implements AsgEBaseContainer<T> {
    //region Constructors
    public CompositeAsgEBasePlanOp() {}

    public CompositeAsgEBasePlanOp(AsgEBase<T> asgElement, Iterable<PlanOp> ops) {
        super(ops);
        this.asgEbase = asgElement;
    }

    public CompositeAsgEBasePlanOp(AsgEBase<T> asgElement, PlanOp...ops) {
        this(asgElement, Stream.of(ops));
    }

    public CompositeAsgEBasePlanOp(AsgEBase<T> asgElement, CompositePlanOp compositePlanOp) {
        this(asgElement, compositePlanOp.getOps());
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.asgEbase.toString() + ")";
    }
    //endregion

    //region Properties
    @Override
    public AsgEBase<T> getAsgEbase() {
        return asgEbase;
    }

    public void setAsgElement(AsgEBase<T> value) {
        this.asgEbase = value;
    }
    //endregion

    //region Fields
    private AsgEBase<T> asgEbase;
    //endregion
}
