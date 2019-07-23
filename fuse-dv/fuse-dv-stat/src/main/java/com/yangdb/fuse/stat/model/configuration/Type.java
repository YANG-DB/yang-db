package com.yangdb.fuse.stat.model.configuration;

/*-
 * #%L
 * fuse-dv-stat
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Type {

    //region Ctrs
    public Type() {
        //needed for Jackson
    }

    public Type(String type, List<Field> fields) {
        this.type = type;
        this.fields = fields;
    }

    public Type(String type) {
        this.type = type;
        this.fields = new ArrayList<>();
    }
    //endregion

    //region Getters & Setters
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("fields")
    public List<Field> getFields() {
        return fields;
    }

    @JsonProperty("fields")
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
    //endregion

    //region Fields
    private String type;
    private List<Field> fields;
    //endregion


    //region Builder
    public static final class Builder {
        //region Fields
        private String typeName;
        private List<Field> fields;

        private Builder() {
            this.fields = new ArrayList<>();
        }

        public static Builder instance() {
            return new Builder();
        }

        public Builder withType(String type) {
            this.typeName = type;
            return this;
        }

        public Builder withFields(List<Field> fields) {
            this.fields = fields;
            return this;
        }

        public Builder withField(Field field) {
            this.fields.add(field);
            return this;
        }

        public Type build() {
            Type type = new Type();
            type.setType(typeName);
            type.setFields(fields);
            return type;
        }
    }
    //endregion


}
