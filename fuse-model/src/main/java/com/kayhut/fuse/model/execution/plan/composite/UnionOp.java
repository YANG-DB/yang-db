package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.quant.QuantBase;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.List;

public class UnionOp extends AsgEBasePlanOp<QuantBase> {

    private AsgEBase<QuantBase> unionStep;
    private List<Plan> plans;

    public UnionOp() {
        super(new AsgEBase<>());
    }

    public UnionOp(AsgEBase<QuantBase> unionStep, List<Iterable<PlanOp>> plans) {
        super(unionStep);
        this.plans = Stream.ofAll(plans).map(Plan::new).toJavaList();
    }

    public UnionOp(AsgEBase<QuantBase> unionStep, List<PlanOp>...plans) {
        super(unionStep);
        this.plans = Stream.ofAll(Arrays.asList(plans)).map(Plan::new).toJavaList();
    }

    public List<Plan> getPlans() {
        return plans;
    }
}
