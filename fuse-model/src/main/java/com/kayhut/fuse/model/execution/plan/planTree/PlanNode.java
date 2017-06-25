package com.kayhut.fuse.model.execution.plan.planTree;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 19/06/2017.
 */

@JsonPropertyOrder({ "name", "desc", "children", "invalidReason" })
public class PlanNode<P> {

    //region Constructors
    public PlanNode() {
        this(null, null, null, null);
    }

    public PlanNode(String planDescription, String planName) {
        this(null, planDescription, null);
    }

    public PlanNode(String planDescription, String planName, String invalidReason) {
        this(null, planDescription, planName, invalidReason);
    }

    public PlanNode(P plan, String planDescription, String planName, String invalidReason) {
        this.children = new ArrayList<>();
        this.plan = plan;
        this.planName  = planName;
        this.planDescription = planDescription;
        this.invalidReason = invalidReason;
    }
    //endregion

    @JsonProperty("name")
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    //region Properties
    @JsonProperty("desc")
    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public String getInvalidReason() {
        return invalidReason != null ? invalidReason : "";
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<PlanNode<P>> getChildren() {
        return children;
    }

    public void setChildren(List<PlanNode<P>> children) {
        this.children = children;
    }

//    public P getPlan() {
//        return plan;
//    }
//
//    public void setPlan(P plan) {
//        this.plan = plan;
//    }


    //endregion

    //region Fields
    private P plan;
    private String planDescription;
    private String planName;
    private String invalidReason;
    private List<PlanNode<P>> children;
    //endregion
}
