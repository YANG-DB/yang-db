package com.yangdb.fuse.model.logical;

/*-
 * #%L
 * fuse-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yangdb.fuse.model.results.Entity;

import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "composite", value = CompositeLogicalNode.class),
        @JsonSubTypes.Type(name = "entity", value = Entity.class),
        @JsonSubTypes.Type(name = "logical", value = LogicalNode.class)
})
public interface Vertex<V extends Vertex> {
    /**
     *
     * @return
     */
    String id();

    /**
     *
     * @return
     */
    String label();

    /**
     *
     * @param label
     * @return
     */
    V label(String label);

    /**
     *
     * @return
     */
    String tag();

    /**
     *
     * @param tag
     * @return
     */
    V tag(String tag);

    /**
     *
     * @param entity
     * @return
     */
    V merge(V entity);

    /**
     *
     * @return
     */
    Map<String,Object> metadata();

    /**
     *
     * @return
     */
    Map<String,Object> fields();

}
