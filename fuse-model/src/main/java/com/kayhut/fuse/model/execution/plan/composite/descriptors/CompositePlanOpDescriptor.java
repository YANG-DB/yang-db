package com.kayhut.fuse.model.execution.plan.composite.descriptors;

import com.google.inject.Inject;
import com.kayhut.fuse.model.descriptors.CompositeDescriptor;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.descriptors.ToStringDescriptor;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositeAsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class CompositePlanOpDescriptor implements Descriptor<CompositePlanOp> {
    //region Constructors
    @Inject
    public CompositePlanOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(CompositePlanOp compositePlanOp) {
        return new StringBuilder()
                .append(compositePlanOp.getClass().getSimpleName())
                .append("[")
                .append(this.planOpsDescriptor.describe(compositePlanOp.getOps()))
                .append("]")
                .toString();
    }
    //endregion

    //region Fields
    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
    //endregion
}