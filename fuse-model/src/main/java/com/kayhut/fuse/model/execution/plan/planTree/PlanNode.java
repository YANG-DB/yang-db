package com.kayhut.fuse.model.execution.plan.planTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 19/06/2017.
 */
public class PlanNode<P> {
    //region Constructors
    public PlanNode() {
        this(null, null, null);
    }

    public PlanNode(String planDescription) {
        this(null, planDescription, null);
    }

    public PlanNode(String planDescription, String invalidReason) {
        this(null, planDescription, invalidReason);
    }

    public PlanNode(P plan, String planDescription, String invalidReason) {
        this.children = new ArrayList<>();
        this.plan = plan;
        this.planDescription = planDescription;
        this.invalidReason = invalidReason;
    }
    //endregion

    //region Properties
    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public List<PlanNode<P>> getChildren() {
        return children;
    }

    public void setChildren(List<PlanNode<P>> children) {
        this.children = children;
    }

    public P getPlan() {
        return plan;
    }

    public void setPlan(P plan) {
        this.plan = plan;
    }
    //endregion

    //region Fields
    private P plan;
    private String planDescription;
    private String invalidReason;

    private List<PlanNode<P>> children;
    //endregion
}
