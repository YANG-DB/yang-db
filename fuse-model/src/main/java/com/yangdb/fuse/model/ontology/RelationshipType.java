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
 * RelationshipType.java - fuse-model - yangdb - 2,016
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 22-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipType implements BaseElement {
    public RelationshipType() {
        properties = new ArrayList<>();
        metadata = new ArrayList<>();
        ePairs = new ArrayList<>();
    }

    public RelationshipType(String name, String rType, boolean directional) {
        this();
        this.rType = rType;
        this.name = name;
        this.directional = directional;
        this.mandatory = new ArrayList<>();

    }

    //region Getters & Setters
    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectional() {
        return directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
    }

    @JsonProperty("DBrName")
    public String getDBrName() {
        return DBrName;
    }

    @JsonProperty("DBrName")
    public void setDBrName(String DBrName) {
        this.DBrName = DBrName;
    }

    public void setePairs(List<EPair> ePairs) {
        this.ePairs = ePairs;
    }

    public List<EPair> getePairs() {
        return ePairs;
    }

    @JsonIgnore
    public Set<String> getSources() {
        return ePairs.stream().map(EPair::geteTypeA).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<String> getTargets() {
        return ePairs.stream().map(EPair::geteTypeB).collect(Collectors.toSet());
    }

    @JsonIgnore
    public RelationshipType addPair(EPair pair) {
        this.getePairs().add(pair);
        return this;
    }

    public List<String> getMetadata() {
        return metadata != null ? metadata : Collections.emptyList();
    }

    public void setMetadata(List<String> metadata) {
        this.metadata = metadata;
    }

    public List<String> getProperties() {
        return properties != null ? properties : Collections.emptyList();
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

    @JsonIgnore
    public RelationshipType addProperty(String type) {
        this.properties.add(type);
        return this;
    }

    @JsonIgnore
    public RelationshipType withProperty(String... properties) {
        this.properties.addAll(Arrays.asList(properties));
        return this;
    }

    @JsonIgnore
    public RelationshipType addMetadata(String type) {
        this.metadata.add(type);
        return this;
    }

    @JsonIgnore
    public RelationshipType withMetadata(String... properties) {
        this.metadata.addAll(Arrays.asList(properties));
        return this;
    }

    @JsonIgnore
    public RelationshipType withEPairs(EPair... pairs) {
        this.setePairs(Arrays.asList(pairs));
        return this;
    }

    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipType that = (RelationshipType) o;
        return directional == that.directional &&
                rType.equals(that.rType) &&
                name.equals(that.name) &&
                mandatory.equals(that.mandatory) &&
                ePairs.equals(that.ePairs) &&
                properties.equals(that.properties) &&
                metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rType, name, directional, mandatory, ePairs, properties, metadata);
    }

    @Override
    public String toString() {
        return "RelationshipType [ePairs = " + ePairs + ", rType = " + rType + ", directional = " + directional + ", name = " + name + ", properties = " + properties + ", metadata = " + metadata + ", mandatory = " + mandatory + "]";
    }

    //region Fields
    private String rType;
    private String name;
    private boolean directional;
    private String DBrName;
    private List<String> mandatory;
    private List<EPair> ePairs;
    private List<String> properties;
    private List<String> metadata;

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
    public boolean hasSideA(String eType) {
        return ePairs.stream().anyMatch(ep -> ep.geteTypeA().equals(eType));
    }

    @JsonIgnore
    public boolean hasSideB(String eType) {
        return ePairs.stream().anyMatch(ep -> ep.geteTypeB().equals(eType));
    }


    //endregion

    //region Builder
    public static final class Builder {
        private String rType;
        private String name;
        private boolean directional;
        private String DBrName;
        private List<String> mandatory = new ArrayList<>();
        private List<EPair> ePairs = new ArrayList<>();
        private List<String> properties = new ArrayList<>();
        private List<String> metatada = new ArrayList<>();

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withRType(String rType) {
            this.rType = rType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDirectional(boolean directional) {
            this.directional = directional;
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


        public Builder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        public Builder withEPairs(List<EPair> ePairs) {
            this.ePairs = ePairs;
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


        public Builder withMetadata(List<String> metatada) {
            this.metatada = metatada;
            return this;
        }

        public RelationshipType build() {
            RelationshipType relationshipType = new RelationshipType();
            relationshipType.setrType(this.rType);
            relationshipType.setName(name);
            relationshipType.setDirectional(directional);
            relationshipType.setDBrName(DBrName);
            relationshipType.setProperties(properties);
            relationshipType.setMetadata(metatada);
            relationshipType.setMandatory(mandatory);
            relationshipType.setePairs(ePairs);
            return relationshipType;
        }
    }
    //endregion

}
