package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.logical.LogicalGraphModel;

import java.util.ArrayList;
import java.util.List;

public class DataTransformerContext {
    private List<ObjectNode> entities;
    private List<ObjectNode> relations;
    private ObjectMapper mapper;
    private LogicalGraphModel graph;

    public DataTransformerContext(ObjectMapper mapper) {
        this.entities = new ArrayList<>();
        this.relations = new ArrayList<>();
        this.mapper = mapper;
    }


    public DataTransformerContext withGraph(LogicalGraphModel graph) {
        this.graph = graph;
        return this;
    }

    public DataTransformerContext withEntities(List<ObjectNode> collect) {
        entities.addAll(collect);
        return this;
    }

    public DataTransformerContext withRelations(List<ObjectNode> collect) {
        relations.addAll(collect);
        return this;
    }

    public List<ObjectNode> getEntities() {
        return entities;
    }

    public List<ObjectNode> getRelations() {
        return relations;
    }

    public LogicalGraphModel getGraph() {
        return graph;
    }

    public final String toString(ObjectMapper mapper) throws JsonProcessingException {
        ObjectNode on = mapper.createObjectNode();
        return mapper.writeValueAsString(collect(mapper, on));
    }

    private Object collect(ObjectMapper mapper, ObjectNode on) {
        return null;
    }
}
