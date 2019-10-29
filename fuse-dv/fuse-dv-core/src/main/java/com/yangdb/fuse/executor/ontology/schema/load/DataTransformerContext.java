package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import java.util.stream.Collectors;

public class DataTransformerContext {
    private List<DocumentBuilder> entities;
    private List<DocumentBuilder> relations;
    private Response transformationResponse;
    private ObjectMapper mapper;
    private LogicalGraphModel graph;

    public DataTransformerContext(ObjectMapper mapper) {
        this.entities = new ArrayList<>();
        this.relations = new ArrayList<>();
        this.mapper = mapper;
        this.transformationResponse = new Response("Transformation");
    }


    public DataTransformerContext withGraph(LogicalGraphModel graph) {
        this.graph = graph;
        return this;
    }

    public DataTransformerContext withEntities(List<DocumentBuilder> collect) {
        //collect errors
        transformationResponse.failure(collect
                .stream()
                .filter(DocumentBuilder::isFailure)
                .map(DocumentBuilder::getError)
                .collect(Collectors.toList()));

        //collect successes
        transformationResponse.success(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .map(DocumentBuilder::getId)
                .collect(Collectors.toList()));

        //collect documents
        entities.addAll(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .collect(Collectors.toList())
        );
        return this;
    }

    public DataTransformerContext withRelations(List<DocumentBuilder> collect) {
        //collect errors
        transformationResponse.failure(collect
                .stream()
                .filter(DocumentBuilder::isFailure)
                .map(DocumentBuilder::getError)
                .collect(Collectors.toList()));

        //collect successes
        transformationResponse.success(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .map(DocumentBuilder::getId)
                .collect(Collectors.toList()));

        //collect documents
        relations.addAll(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .collect(Collectors.toList())
        );
        return this;
    }

    public List<DocumentBuilder> getEntities() {
        return entities;
    }

    public List<DocumentBuilder> getRelations() {
        return relations;
    }

    public LogicalGraphModel getGraph() {
        return graph;
    }

    public Response getTransformationResponse() {
        return transformationResponse;
    }
}
