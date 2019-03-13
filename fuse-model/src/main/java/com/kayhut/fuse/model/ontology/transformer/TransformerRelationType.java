package com.kayhut.fuse.model.ontology.transformer;

/*-
 * #%L
 * EntityType.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
public class TransformerRelationType {
    public TransformerRelationType() {
    }

    public TransformerRelationType(String type, String name, List<Map<String,String>> metadataProperties) {
        this.rType = type;
        this.pattern = name;
        this.metadataProperties = metadataProperties;
    }

    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public TransformerProperties getProperties() {
        return properties;
    }

    public void setProperties(TransformerProperties properties) {
        this.properties = properties;
    }

    public List<Map<String,String>> getMetadataProperties() {
        return metadataProperties;
    }

    public void setMetadataProperties(List<Map<String,String>> metadataProperties) {
        this.metadataProperties = metadataProperties;
    }


    @Override
    public String toString()
    {
        return "EntityType [eType = "+ rType +", name = "+ pattern +", properties = "+ metadataProperties +"]";
    }

    //region Fields
    private String rType;
    private String pattern;
    private List<Map<String,String>> metadataProperties;
    private TransformerProperties properties;
    //endregion


}
