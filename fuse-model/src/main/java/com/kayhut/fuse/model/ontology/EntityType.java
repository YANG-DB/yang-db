package com.kayhut.fuse.model.ontology;

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

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EntityType {
    public EntityType() {
    }

    public EntityType(String type, String name, List<String> properties) {
        this.eType = type;
        this.name = name;
        this.properties = properties;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public List<String> getDisplay() {
        return display;
    }

    public void setDisplay(List<String> display) {
        this.display = display;
    }

    @Override
    public String toString()
    {
        return "EntityType [eType = "+eType+", name = "+name+", display = "+display+", properties = "+properties+"]";
    }

    //region Fields
    private String eType;
    private String name;
    private List<String> properties;
    private List<String> display;
    //endregion

    //region Builder
    public static final class Builder {
        private String eType;
        private String name;
        private List<String> properties;
        private List<String> display;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withEType(String eType) {
            this.eType = eType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        public Builder withDisplay(List<String> display) {
            this.display = display;
            return this;
        }

        public EntityType build() {
            EntityType entityType = new EntityType();
            entityType.setName(name);
            entityType.setProperties(properties);
            entityType.setDisplay(display);
            entityType.eType = this.eType;
            return entityType;
        }
    }
    //endregion

}
