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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.results.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class KnowledgeDomainBuilder {
    public static final String DEFAULT_CTX = "default";

    public abstract String getType();

    public abstract String id();

    public Optional<String> routing() {
        return Optional.empty();
    }

    public final String toString(ObjectMapper mapper) throws JsonProcessingException {
        ObjectNode on = mapper.createObjectNode();
        return mapper.writeValueAsString(collect(mapper,on));
    }

    public abstract Entity toEntity();

    public abstract String getETag();

    public abstract ObjectNode collect(ObjectMapper mapper, ObjectNode node);


    protected ArrayNode collectRefs(ObjectMapper mapper, List<String> refs) {
        ArrayNode refsNode = mapper.createArrayNode();
        for (String ref : refs) {
            refsNode.add(ref);
        }
        return refsNode;
    }

    public List<KnowledgeDomainBuilder> additional() {
        return Collections.emptyList();
    }



}
