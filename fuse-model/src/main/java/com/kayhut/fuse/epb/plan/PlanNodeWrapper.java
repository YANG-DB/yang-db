package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;

import java.util.Optional;

/**
 * Created by liorp on 6/25/2017.
 */
public interface PlanNodeWrapper<P extends IPlan> {
    Optional<PlanNode<P>> planNode();
}
