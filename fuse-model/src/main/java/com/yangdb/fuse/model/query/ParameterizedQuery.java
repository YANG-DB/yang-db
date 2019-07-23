package com.yangdb.fuse.model.query;

/*-
 * #%L
 * ParameterizedQuery.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;

import java.util.Collection;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = ParameterizedQuery.Builder.class)
public class ParameterizedQuery extends Query {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<NamedParameter> params;

    public ParameterizedQuery(Query query,Collection<NamedParameter> params) {
        this.params = params;
        this.setElements(query.getElements());
        this.setName(query.getName());
        this.setOnt(query.getOnt());
        this.setNonidentical(query.getNonidentical());
    }

    public Collection<NamedParameter> getParams() {
        return params;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        private Collection<NamedParameter> params;
        private Query.Builder builder;

        private Builder() {
            builder = Query.Builder.instance();
        }

        public static ParameterizedQuery.Builder instance() {
            return new ParameterizedQuery.Builder();
        }

        public ParameterizedQuery.Builder withOnt(String ont) {
            this.builder.withOnt(ont);
            return this;
        }

        public ParameterizedQuery.Builder withName(String name) {
            this.builder.withName(name);
            return this;
        }

        public ParameterizedQuery.Builder withElements(List<EBase> elements) {
            this.builder.withElements(elements);
            return this;
        }

        public ParameterizedQuery.Builder appendElements(List<EBase> elements) {
            this.builder.appendElements(elements);
            return this;
        }

        public ParameterizedQuery.Builder withElement(EBase ... element) {
            this.builder.withElement(element);
            return this;
        }

        public ParameterizedQuery.Builder withParams(Collection<NamedParameter> params) {
            this.params = params;
            return this;
        }

        public ParameterizedQuery.Builder withNonidentical(List<List<String>> nonidentical) {
            this.builder.withNonidentical(nonidentical);
            return this;
        }

        public ParameterizedQuery build() {
            return new ParameterizedQuery(this.builder.build(),params);
        }

    }

}
