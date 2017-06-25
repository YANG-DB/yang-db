package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;

/**
 * Created by liorp on 6/25/2017.
 */
public interface PlanNodeWrapper<P extends IPlan> {
    PlanNode<P> planNode();
}
