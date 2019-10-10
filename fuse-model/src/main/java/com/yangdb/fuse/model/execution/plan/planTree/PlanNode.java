package com.yangdb.fuse.model.execution.plan.planTree;

/*-
 *
 * PlanNode.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yangdb.fuse.model.execution.plan.IPlan;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Roman on 19/06/2017.
 */

@JsonPropertyOrder({ "name", "desc", "children", "invalidReason" })
public class PlanNode<P extends IPlan > {

    public static final String PLAN_VERBOSE = "planVerbose";

    //region Constructors
    public PlanNode(int phase, String id, String planDescription, String display, String invalidReason) {
        this.phase = phase;
        this.id = id;
        this.children = new ArrayList<>();
        this.planName = display;
        this.planDescription = planDescription;
        this.invalidReason = invalidReason;
    }
    //endregion



    //region Properties

    @JsonProperty("name")
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
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

    @JsonIgnore
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @JsonIgnore
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

    public static class Builder<P extends IPlan> implements BuilderIfc<P> {
        private final ConcurrentHashMap<String, PlanNode> levelMap = new ConcurrentHashMap<>();
        private static final BuilderIfc MOCK = new BuilderIfc() {

            @Override
            public BuilderIfc add(PlanNode child) {
                return this;
            }

            @Override
            public BuilderIfc add(IPlan node, String validationContext) {
                return this;
            }

            @Override
            public int incAndGetPhase() {
                return 0;
            }

            @Override
            public BuilderIfc with(IPlan node) {
                return this;
            }

            @Override
            public BuilderIfc selected(Iterable selectedPlans) {
                return this;
            }

            @Override
            public Optional<PlanNode> build() {
                return Optional.empty();
            }
        };

        private PlanNode root;
        private PlanNode context;
        private int phase;


        public static BuilderIfc root(String query) {
            if(MDC.get(PLAN_VERBOSE)!=null)
                return new PlanNode.Builder(query);
            return MOCK;
        }

        private Builder(String query) {
            phase = -1;
            root = new PlanNode(phase,"root",query,-1+"","valid");
            context = root;
            levelMap.put(root.hashCode()+"", root);
            incAndGetPhase();
        }

        /**
         * add child plan node
         * @param child
         * @return
         */
        @Override
        public BuilderIfc add(PlanNode child) {
            context.children.add(child);
            levelMap.put(child.getId(), child);
            return this;
        }

        @Override
        public BuilderIfc add(P node, String validationContext) {
            return add(new PlanNode(phase, node.hashCode() + "", node.toString(), phase + "", validationContext));
        }

        @Override
        public int incAndGetPhase() {
            phase++;
            return phase;
        }

        @Override
        public BuilderIfc with(P node) {
            context = levelMap.get(node.hashCode() + "");
            return this;
        }

        @Override
        public BuilderIfc selected(Iterable<P> selectedPlans) {
            selectedPlans.forEach(p -> {
                levelMap.get(p.hashCode() + "").setSelected(true);
            });
            return this;
        }

        @Override
        public Optional<PlanNode<P>> build() {
            return Optional.of(root);
        }
    }
    //endregion
}
