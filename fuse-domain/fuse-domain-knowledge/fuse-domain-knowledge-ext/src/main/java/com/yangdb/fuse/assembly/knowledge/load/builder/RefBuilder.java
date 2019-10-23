package com.yangdb.fuse.assembly.knowledge.load.builder;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Arrays;

public class RefBuilder extends Metadata {
    public static final String REF_INDEX = "ref0";

    public static final String type = "Reference";
    public static String physicalType = "reference";

    public String title;
    public String url;
    public String content;
    public String system;
    private String refId;

    public static RefBuilder _ref(String id) {
        final RefBuilder builder = new RefBuilder();
        builder.refId = id;
        return builder;
    }

    public RefBuilder title(String title) {
        this.title = title;
        return this;
    }

    public RefBuilder content(String content) {
        this.content = content;
        return this;
    }

    public RefBuilder url(String url) {
        this.url = url;
        return this;
    }

    public RefBuilder sys(String sys) {
        this.system = sys;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return refId;
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        //create knowledge entity
        on.put("id", id());
        on.put("type", physicalType);
        on.put("title", title);
        on.put("url", url);
        on.put("system", system);
        //make sure value or content
        if (content != null) on.put("content", content);
        return on;
    }

    @Override
    public Entity
    toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Stream.ofAll(Arrays.asList(
                        new Property("title", "raw", title),
                        new Property("content", "raw", content),
                        new Property("url", "raw", url),
                        new Property("system", "raw", system)))
                        .filter(p -> p.getValue() != null).toJavaList()
                )).build();
    }

    @Override
    public String getETag() {
        return "Reference." + id();
    }
}
