package com.kayhut.fuse.model.query;

/*-
 * #%L
 * Query.java - fuse-model - kayhut - 2,016
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.query.entity.EEntityBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = Query.Builder.class)
public class Query implements IQuery<EBase> {

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EBase> getElements() {
        return elements;
    }

    public void setElements(List<EBase> elements) {
        this.elements = elements;
    }

    public List<List<String>> getNonidentical() {
        return nonidentical;
    }

    public void setNonidentical(List<List<String>> nonidentical) {
        this.nonidentical = nonidentical;
    }

    //region Fields
    private String ont;
    private String name;
    private List<List<String>> nonidentical;
    private List<EBase> elements = new ArrayList<>();
    //endregion

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        private String ont;
        private String name;
        private List<EBase> elements;
        private List<List<String>> nonidentical;

        private Builder() {
        }

        public static Builder instance() {
            return new Builder();
        }

        public Builder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withElements(List<EBase> elements) {
            this.elements = elements;
            return this;
        }

        public Builder withElement(EBase ... element) {
            if(elements==null) {
                this.elements = new ArrayList<>();
            }
            this.elements.addAll(Arrays.asList(element));
            return this;
        }

        public Builder withNonidentical(List<List<String>> nonidentical) {
            this.nonidentical = nonidentical;
            return this;
        }

        public Query build() {
            Query query = new Query();
            query.setOnt(ont);
            query.setName(name);
            if (elements != null)
                query.setElements(elements);
            if (nonidentical != null)
                query.setNonidentical(nonidentical);
            return query;
        }

    }

    public static class QueryUtils {
        /**
         * find query element by its enum
         *
         * @param query
         * @param eNum
         * @return
         */
        public static Optional<? extends EBase> findByEnum(IQuery<EBase> query, int eNum) {
            return query.getElements().stream().filter(p -> p.geteNum() == eNum).findFirst();
        }

        /**
         * @param query
         * @return
         */
        public static <T extends EBase> List<EBase> findByClass(IQuery<EBase> query, Class<T> klass) {
            return query.getElements().stream().filter(p -> klass.isAssignableFrom(p.getClass())).collect(Collectors.toList());
        }

        /**
         * find element by tag name
         * @param query
         * @param tag
         * @return
         */
        public static Optional<? extends EBase> findByTag(IQuery<EBase> query, String tag) {
            return query.getElements().stream()
                    .filter(p -> p.getClass().isAssignableFrom(EEntityBase.class))
                    .filter(p -> ((EEntityBase)p).geteTag().equals(tag))
                    .findFirst();
        }
    }
}
