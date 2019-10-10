package com.yangdb.fuse.model.ontology;

/*-
 *
 * Property.java - fuse-model - yangdb - 2,016
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

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Property {
    //region Constructors
    public Property() {
    }

    public Property(String name, String pType, String type) {
        this.pType = pType;
        this.name = name;
        this.type = type;
    }
    //endregion

    //region Properties
    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
        this.pType = pType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return String.format("Property [pType = %s, name = %s, type = %s]", this.pType, this.name, this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Property other = (Property) o;

        return this.pType.equals(other.pType) &&
                this.name.equals(other.name) &&
                this.type.equals(other.type);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.pType.hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }
    //endregion

    //region Fields
    private String pType;
    private String name;
    private String type;
    //endregion

    //region Builder
    public static final class Builder {
        private String pType;
        private String name;
        private String type;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withPType(String pType) {
            this.pType = pType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Property build(String pType, String name, String type ) {
            Property property = new Property();
            property.setName(name);
            property.setType(type);
            property.setpType(pType);
            return property;
        }

        public Property build() {
            Property property = new Property();
            property.setName(name);
            property.setType(type);
            property.pType = this.pType;
            return property;
        }
    }
    //endregion



}
