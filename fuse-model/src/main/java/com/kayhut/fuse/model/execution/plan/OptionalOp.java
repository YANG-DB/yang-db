package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import javaslang.collection.Stream;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 23/02/2017.
 */
public class OptionalOp extends CompositePlanOpBase {
    //region Constructors
    private OptionalOp() {}

    public OptionalOp(PlanOpBase...ops) {
        this(Stream.of(ops));
    }

    public OptionalOp(CompositePlanOpBase compositePlanOp) {
        this(compositePlanOp.getOps());
    }

    public OptionalOp(Iterable<PlanOpBase> ops) {
        super(ops);
    }
    //endregion

    @Override
    public int geteNum() {
        return 0;
    }
    //endregion
}
