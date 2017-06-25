package com.kayhut.fuse.model.execution.plan.planTree;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.kayhut.fuse.model.execution.plan.IPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Roman on 19/06/2017.
 */

@JsonPropertyOrder({ "name", "desc", "children", "invalidReason" })
public class PlanNode<P extends IPlan > {

    //region Constructors
    public PlanNode(int phase,String id, String planDescription, String display, String invalidReason) {
        this.phase = phase;
        this.id = id;
        this.children = new ArrayList<>();
        this.planName  = display;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    //endregion

    //region Fields
    private boolean selected;
    private String planDescription;
    private String planName;
    private String id;
    private int phase;
    private String invalidReason;
    private List<PlanNode<P>> children;

    public static class Builder<P extends IPlan> {
        private final ConcurrentHashMap<String,PlanNode> levelMap = new ConcurrentHashMap<>();
        private PlanNode root;
        private PlanNode context;
        private int phase;

        private Builder(String query) {
            phase = -1;
            root = new PlanNode(phase,"root",query,-1+"","valid");
            context = root;
            levelMap.put(root.hashCode()+"", root);
            incAndGetPhase();
        };

        public static Builder root(String query) {
            return new Builder(query);
        }

        /**
         * add child plan node
         * @param child
         * @return
         */
        public Builder add(PlanNode child) {
            context.children.add(child);
            levelMap.put(child.getId(),child);
            return this;
        }

        public Builder add(P node,  String validationContext) {
            return add(new PlanNode(phase,node.hashCode()+"",node.toString(),phase+"", validationContext));
        }

        public int incAndGetPhase() {
            phase++;
            return phase;
        }

        public Builder with(P node) {
            context = levelMap.get(node.hashCode()+"");
            return this;
        }

        public Builder selected(Iterable<P> selectedPlans) {
            selectedPlans.forEach(p-> {
                levelMap.get(p.hashCode()+"").setSelected(true);
            });
            return this;
        }

        public PlanNode build() {
            return root;
        }
    }
    //endregion
}
