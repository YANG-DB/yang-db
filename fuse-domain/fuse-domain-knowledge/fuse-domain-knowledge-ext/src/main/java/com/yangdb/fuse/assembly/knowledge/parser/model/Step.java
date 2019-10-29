package com.yangdb.fuse.assembly.knowledge.parser.model;

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

import com.yangdb.fuse.model.execution.plan.Direction;

import java.util.Collections;
import java.util.Map;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Step {

    private String conceptId;
    private ElementType type;
    private Direction direction;
//    @JsonIgnoreProperties(ignoreUnknown = true)
    private Map<String, Property> properties = Collections.emptyMap();


    public String getConceptId() {
        return conceptId;
    }

    public Step setConceptId(String conceptId) {
        this.conceptId = conceptId;
        return this;
    }

    public ElementType getType() {
        return type;
    }

    public Step setType(ElementType type) {
        this.type = type;
        return this;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public Step setProperties(Map<String, Property> properties) {
        this.properties = properties;
        return this;
    }

    public Direction getDirection() {
        return direction;
    }
}
