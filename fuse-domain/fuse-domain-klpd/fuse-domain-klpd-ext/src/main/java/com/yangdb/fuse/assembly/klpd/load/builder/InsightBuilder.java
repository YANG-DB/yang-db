package com.yangdb.fuse.assembly.klpd.load.builder;

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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsightBuilder extends EntityId {
    public static final String INSIGHT_INDEX = "i0";
    public static final String type = "Insight";
    public static String physicalType = "insight";

    public String content;
    public String context = DEFAULT_CTX;
    public List<String> entityIds;
    private String insightId;

    public static InsightBuilder _i(String id) {
        final InsightBuilder builder = new InsightBuilder();
        builder.insightId = id;
        return builder;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return insightId;
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);

        ArrayNode entities = mapper.createArrayNode();
        for (String ent : entityIds) {
            entities.add(ent);
        }
        //create knowledge entity
        on.put("type", physicalType);
        on.put("content", content);
        on.put("context", context);
        on.put("entityIds", entities);

        return on;
    }

    public InsightBuilder entityIds(List<String> entityIds) {
        this.entityIds = entityIds;
        return this;
    }

    public InsightBuilder context(String context) {
        this.context = context;
        return this;
    }

    public InsightBuilder content(String content) {
        this.content = content;
        return this;
    }


    @Override
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        new Property("content", "raw", content),
                        new Property("context", "raw", context),
                        new Property("entityIds", "raw", !entityIds.isEmpty() ? new ArrayList<>(entityIds) : null))
                )).build();
    }

    @Override
    public String getETag() {
        return "Insight."+id();
    }

    public static class EntityInsightBuilder extends KnowledgeDomainBuilder {
        public static String physicalType = "e.insight";

        private final String logicalId;
        private final String context;
        private final String insightId;

        public EntityInsightBuilder(String logicalId,String context, String insightId) {
            this.logicalId = logicalId;
            this.context = context;
            this.insightId = insightId;
        }

        @Override
        public String getType() {
            return "e.insight";
        }

        @Override
        public String id() {
            return logicalId+"."+insightId;
        }

        @Override
        public Entity toEntity() {
            return null;
        }

        @Override
        public String getETag() {
            return "";
        }

        @Override
        public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
            node.put("id", id());
            node.put("type", physicalType);
            node.put("entityId",logicalId+"."+context);
            node.put("insightId",insightId);
            return node;
        }
    }
}
