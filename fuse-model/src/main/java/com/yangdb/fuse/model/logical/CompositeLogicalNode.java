package com.yangdb.fuse.model.logical;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class CompositeLogicalNode extends LogicalNode {

    @JsonProperty("children")
    private Map<String,LogicalNode> children;

    public CompositeLogicalNode() {
        this.children = new HashMap<>();
    }

    public CompositeLogicalNode(String id,String label) {
        super(id,label);
        this.children = new HashMap<>();
    }

    @JsonIgnore
    public CompositeLogicalNode withChild(String key,LogicalNode child) {
        this.children.put(key,child);
        return this;
    }

    @JsonProperty("children")
    public Map<String, LogicalNode> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(Map<String, LogicalNode> children) {
        this.children = children;
    }
}
