package com.yangdb.fuse.model.logical;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
 * #L%
 */

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
