package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 *
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
 *
 */

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.Tuple2;

import java.util.Map;
import java.util.Optional;

public class DocumentBuilder {
    private ObjectNode node;
    private String id;
    private String type;
    private Optional<String> routing;
    private Optional<Tuple2<String,String>> partitionField;
    private FuseError error;

    public DocumentBuilder(FuseError error) {
        this.error = error;
    }

    public DocumentBuilder(ObjectNode node, String id, String type) {
        this(node,id,type,Optional.empty());
    }

    public DocumentBuilder(ObjectNode node, String id, String type, Optional<String> routing) {
        this(node,id,type,routing,Optional.empty());
    }

    public DocumentBuilder(ObjectNode node, String id, String type, Optional<String> routing, Optional<Tuple2<String,String>> partitionField) {
        this.node = node;
        this.id = id;
        this.type = type;
        this.routing = routing;
        this.partitionField = partitionField;
    }


    public ObjectNode getNode() {
        return node;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Optional<String> getRouting() {
        return routing;
    }

    public Optional<Tuple2<String,String>> getPartitionField() {
        return partitionField;
    }


    public boolean isSuccess() {
        return getError()==null;
    }

    public boolean isFailure() {
        return getError()!=null;
    }

    public FuseError getError() {
        return error;
    }
}
