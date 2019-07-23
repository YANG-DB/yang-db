package com.yangdb.fuse.model.ontology.transformer;

/*-
 * #%L
 * fuse-model
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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransformerProperties {
    private String pattern;
    private String label;
    private String concreteType;
    private List<Map<String,String>> valuePatterns;

    public TransformerProperties() {}

    public TransformerProperties(String pattern,String label, String eType, List<Map<String, String>> valuePatterns) {
        this.label = label;
        this.pattern = pattern;
        this.concreteType = eType;
        this.valuePatterns = valuePatterns;
    }

    public String getLabel() {
        return label;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getConcreteType() {
        return concreteType;
    }

    public void setConcreteType(String concreteType) {
        this.concreteType = concreteType;
    }

    public List<Map<String,String>> getValuePatterns() {
        return valuePatterns;
    }

    public void setValuePatterns(List<Map<String,String>> valuePatterns) {
        this.valuePatterns = valuePatterns;
    }
}
