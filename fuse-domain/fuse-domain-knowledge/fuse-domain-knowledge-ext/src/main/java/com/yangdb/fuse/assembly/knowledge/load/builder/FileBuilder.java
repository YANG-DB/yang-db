package com.yangdb.fuse.assembly.knowledge.load.builder;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Optional;

public class FileBuilder extends EntityId {
    public static final String type = "Efile";
    public static final String physicalType = "e.file";
    public String fileId;
    public String name;
    public String path;
    public String displayName;
    public String mimeType;
    public String category;
    public String description;
    public String context = DEFAULT_CTX;

    public static FileBuilder _f(String fileId) {
        final FileBuilder builder = new FileBuilder();
        builder.fileId = fileId;
        return builder;
    }

    public FileBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public FileBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FileBuilder logicalId(String logicalId) {
        this.logicalId = logicalId;
        return this;
    }

    public FileBuilder display(String display) {
        this.displayName = display;
        return this;
    }

    public FileBuilder path(String path) {
        this.path = path;
        return this;
    }

    public FileBuilder mime(String mime) {
        this.mimeType = mime;
        return this;
    }

    public FileBuilder desc(String desc) {
        this.description = desc;
        return this;
    }

    public FileBuilder ctx(String context) {
        this.context = context;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return fileId;
    }

    @Override
    public Optional<String> routing() {
        return Optional.of(logicalId);
    }

    @Override
    /**
     * compare purpose api
     */
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        on.put("id", id());
        on.put("type", physicalType);
        on.put("name", name);
        on.put("path", path);
        on.put("displayName", displayName);
        on.put("mimeType", mimeType);
        on.put("category", category);
        on.put("description", description);
        on.put("logicalId", logicalId);
        on.put("entityId", entityId);
        on.put("context", context);
        return on;
    }

    @Override
    public String getETag() {
        return "File." + id();
    }

    /**
     * write purpose api
     */
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(fileId)
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        new Property("name", "raw", name),
                        new Property("path", "raw", path),
                        new Property("displayName", "raw", displayName),
                        new Property("mimeType", "raw", mimeType),
                        new Property("category", "raw", category),
                        new Property("description", "raw", description),
                        new Property("logicalId", "raw", logicalId),
                        new Property("context", "raw", context)
                ))).build();
    }

}
