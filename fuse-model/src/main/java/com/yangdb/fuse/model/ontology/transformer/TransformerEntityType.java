package com.yangdb.fuse.model.ontology.transformer;

/*-
 *
 * EntityType.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransformerEntityType {
    public TransformerEntityType() {
    }

    public TransformerEntityType(String id,String label,String type, String name, List<Map<String,String>> metadataProperties) {
        this.id = id;
        this.label = label;
        this.eType = type;
        this.pattern = name;
        this.metadataProperties = metadataProperties;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
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

    public boolean hasMetadataProperty(String key) {
        return this.getMetadataProperties().stream().filter(map -> map.containsKey(key)).findAny().isPresent();
    }

    public Optional<Map<String, String>> metadataProperty(String key) {
        return this.getMetadataProperties().stream().filter(map -> map.containsKey(key)).findAny();
    }

    @Override
    public String toString()
    {
        return "EntityType [id = "+id+",eType = "+eType+", name = "+ pattern +", label = "+ label +", properties = "+ metadataProperties +"]";
    }

    private String label;
    //region Fields
    private String eType;
    private String id;
    private String pattern;
    private List<Map<String,String>> metadataProperties;
    private TransformerProperties properties;

    //endregion


}
