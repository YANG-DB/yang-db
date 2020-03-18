package com.yangdb.fuse.model.ontology;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityType {
    public EntityType() {
    }

    public EntityType(String type, String name, List<String> properties, List<String> metadata) {
        this.eType = type;
        this.name = name;
        this.properties = properties;
        this.metadata = metadata;
        this.mandatory = new ArrayList<>();
    }

    public EntityType(String type, String name, List<String> properties, List<String> metadata, List<String> mandatory) {
        this.eType = type;
        this.name = name;
        this.properties = properties;
        this.metadata = metadata;
        this.mandatory = mandatory;
    }

    public EntityType(String type, String name, List<String> properties) {
        this.eType = type;
        this.name = name;
        this.properties = properties;
        this.metadata = new ArrayList<>();
        this.mandatory = new ArrayList<>();
    }

    public List<String> getMetadata() {
        return metadata!=null ? metadata : Collections.emptyList();
    }

    public void setMetadata(List<String> metadata) {
        this.metadata = metadata;
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
        return properties!=null ? properties :  Collections.emptyList();
    }

    public List<String> getMandatory() {
        return mandatory!=null ? mandatory :  Collections.emptyList();
    }

    public void setMandatory(List<String> mandatory) {
        this.mandatory = mandatory;
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
    public String toString() {
        return "EntityType [eType = " + eType + ", name = " + name + ", display = " + display + ", properties = " + properties + ", metadata = " + metadata +", mandatory = " + mandatory + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType that = (EntityType) o;
        return eType.equals(that.eType) &&
                name.equals(that.name) &&
                properties.equals(that.properties) &&
                Objects.equals(metadata, that.metadata) &&
                display.equals(that.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eType, name, properties, metadata, display);
    }

    //region Fields
    private String eType;
    private String name;
    private List<String> mandatory;
    private List<String> properties;
    private List<String> metadata;
    private List<String> display;

    @JsonIgnore
    public boolean containsMetadata(String key) {
        return metadata.contains(key);
    }

    @JsonIgnore
    public boolean isMandatory(String key) {
        return mandatory.contains(key);
    }

    @JsonIgnore
    public boolean containsProperty(String key) {
        return properties.contains(key);
    }
    //endregion

    //region Builder
    public static final class Builder {
        private String eType;
        private String name;
        private List<String> mandatory = new ArrayList<>();
        private List<String> properties = new ArrayList<>();
        private List<String> metadata = new ArrayList<>();
        private List<String> display = new ArrayList<>();

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

        public Builder withProperty(String property) {
            this.properties.add(property);
            return this;
        }

        public Builder withMandatory(List<String> mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        public Builder withMandatory(String mandatory) {
            this.mandatory.add(mandatory);
            return this;
        }

        public Builder withMetadata(List<String> metadata) {
            this.metadata = metadata;
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
            entityType.setMandatory(mandatory);
            entityType.setMetadata(metadata);
            entityType.setDisplay(display);
            entityType.eType = this.eType;
            return entityType;
        }
    }
    //endregion

}
