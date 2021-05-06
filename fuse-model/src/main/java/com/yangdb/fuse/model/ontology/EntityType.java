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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.model.GlobalConstants;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Streams.concat;
import static java.util.Collections.singletonList;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityType implements BaseElement {
    public EntityType() {
    }

    public EntityType(String type, String name, List<String> properties, List<String> metadata) {
        this(type, name, properties, metadata, Collections.emptyList(), Collections.emptyList());
    }

    public EntityType(String type, String name, List<String> properties, List<String> metadata, List<String> mandatory, List<String> parentType) {
        this.eType = type;
        this.name = name;
        this.properties = properties;
        this.metadata = metadata;
        this.mandatory = mandatory;
        this.parentType = parentType;
    }

    public EntityType(String type, String name, List<String> properties) {
        this(type, name, properties, Collections.emptyList());
    }

    public List<String> getMetadata() {
        return metadata != null ? metadata : Collections.emptyList();
    }

    public void setMetadata(List<String> metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("DBrName")
    public String getDBrName() {
        return DBrName;
    }

    @JsonProperty("DBrName")
    public void setDBrName(String DBrName) {
        this.DBrName = DBrName;
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
        return properties != null ? properties : Collections.emptyList();
    }

    public List<String> getParentType() {
        return parentType != null ? parentType : Collections.emptyList();
    }

    public void setParentType(List<String> parentType) {
        this.parentType = parentType;
    }

    public List<String> getMandatory() {
        return mandatory != null ? mandatory : Collections.emptyList();
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

    public List<String> getIdField() {
        return idField;
    }

    public void setIdField(List<String> idField) {
        this.idField = idField;
    }

    @Override
    public String toString() {
        return "EntityType [idField = " + idField + ",eType = " + eType + ", name = " + name + ", display = " + display + ", properties = " + properties + ", metadata = " + metadata + ", mandatory = " + mandatory + "]";
    }

    @JsonIgnore
    public String idFieldName() {
        return BaseElement.idFieldName(getIdField());
    }

    @JsonIgnore
    public List<String> fields() {
        return concat(properties.stream(), metadata.stream()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType that = (EntityType) o;
        return  idField.equals(that.idField) &&
                eType.equals(that.eType) &&
                Objects.equals(parentType, that.parentType) &&
                name.equals(that.name) &&
                properties.equals(that.properties) &&
                Objects.equals(metadata, that.metadata) &&
                display.equals(that.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idField,eType, parentType, name, properties, metadata, display);
    }

    //region Fields
    private List<String> idField = singletonList(GlobalConstants.ID);
    private String eType;
    private String name;
    private String DBrName;
    private List<String> mandatory = new ArrayList<>();
    private List<String> properties = new ArrayList<>();
    private List<String> metadata = new ArrayList<>();
    private List<String> display = new ArrayList<>();
    private List<String> parentType = new ArrayList<>();

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

    @JsonIgnore
    public boolean containsSuperType(String key) {
        return parentType.contains(key);
    }
    //endregion

    //region Builder
    public static final class Builder {
        private List<String> idField = new ArrayList<>();
        private String eType;
        private String name;
        private String DBrName;
        private List<String> mandatory = new ArrayList<>();
        private List<String> properties = new ArrayList<>();
        private List<String> metadata = new ArrayList<>();
        private List<String> display = new ArrayList<>();
        private List<String> parentType = new ArrayList<>();

        private Builder() {
            idField.add(GlobalConstants.ID);
        }


        public static Builder get() {
            return new Builder();
        }

        @JsonIgnore
        public Builder withIdField(String ... idField) {
            //only populate if fields are not empty so that the default GlobalConstants.ID would not vanish
            if(idField.length>0) this.idField = Arrays.asList(idField);
            return this;
        }

        @JsonIgnore
        public Builder withEType(String eType) {
            this.eType = eType;
            return this;
        }

        @JsonIgnore
        public Builder withParentType(List<String> superTypes) {
            this.parentType = superTypes;
            return this;
        }

        @JsonIgnore
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        @JsonIgnore
        public Builder withProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        @JsonIgnore
        public Builder withProperty(String property) {
            this.properties.add(property);
            return this;
        }

        @JsonIgnore
        public Builder withMandatory(List<String> mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        @JsonIgnore
        public Builder withMandatory(String mandatory) {
            this.mandatory.add(mandatory);
            return this;
        }

        @JsonIgnore
        public Builder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        @JsonIgnore
        public Builder withMetadata(List<String> metadata) {
            this.metadata = metadata;
            return this;
        }

        @JsonIgnore
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
            entityType.setParentType(parentType);
            entityType.eType = this.eType;
            entityType.idField = this.idField;
            entityType.DBrName = this.DBrName;
            return entityType;
        }
    }
    //endregion

}
