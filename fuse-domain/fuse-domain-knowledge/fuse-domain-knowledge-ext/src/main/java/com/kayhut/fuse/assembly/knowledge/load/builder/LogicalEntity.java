package com.kayhut.fuse.assembly.knowledge.load.builder;

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

import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Collections;

public class LogicalEntity extends EntityId {
    public static final String type = "LogicalEntity";
    private String logicalId;

    public LogicalEntity(String logicalId) {
        this.logicalId = logicalId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return logicalId;
    }

    @Override
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withProperties(Collections.singletonList(new Property("logicalId", "raw", logicalId)))
                .withEType("LogicalEntity").build();
    }

    @Override
    public String getETag() {
        return "LogicalEntity";
    }
}
