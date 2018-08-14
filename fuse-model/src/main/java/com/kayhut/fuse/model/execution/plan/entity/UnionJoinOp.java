package com.kayhut.fuse.model.execution.plan.entity;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

import java.util.Arrays;
import java.util.List;

public class UnionJoinOp extends EntityOp {

    public UnionJoinOp() {
        super(new AsgEBase<>());
    }

    public UnionJoinOp(Plan common, Plan... branches) {
        this.common = common;
        this.branches = Arrays.asList(branches);
    }

    public List<Plan> getBranches() {
        return branches;
    }

    public Plan getCommon() {
        return common;
    }

    private List<Plan> branches;
    private Plan common;
}
