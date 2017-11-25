package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import javaslang.collection.Stream;

/**
 * Created by User on 23/02/2017.
 */
public class OptionalOp extends CompositeAsgEBasePlanOp<OptionalComp> {
    //region Constructors
    public OptionalOp(AsgEBase<OptionalComp> asgEBase, Iterable<PlanOp> ops) {
        super(asgEBase, ops);
    }

    public OptionalOp(AsgEBase<OptionalComp> asgEBase, PlanOp...ops) {
        this(asgEBase, Stream.of(ops));
    }

    public OptionalOp(AsgEBase<OptionalComp> asgEBase, CompositePlanOp compositePlanOp) {
        this(asgEBase, compositePlanOp.getOps());
    }
    //endregion
}
