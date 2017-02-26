package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;

/**
 * Created by user on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder=Query.QueryBuilder.class)
public class Query {

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
    private List<EBase> elements;
    private List<List<String>> nonidentical;
    //endregion

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class QueryBuilder {
        private String ont;
        private String name;
        private List<EBase> elements;
        private List<List<String>> nonidentical;

        private QueryBuilder() {
        }

        public static QueryBuilder aQuery() {
            return new QueryBuilder();
        }

        public QueryBuilder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public QueryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public QueryBuilder withElements(List<EBase> elements) {
            this.elements = elements;
            return this;
        }

        public QueryBuilder withNonidentical(List<List<String>> nonidentical) {
            this.nonidentical = nonidentical;
            return this;
        }

        public Query build() {
            Query query = new Query();
            query.setOnt(ont);
            query.setName(name);
            query.setElements(elements);
            query.setNonidentical(nonidentical);
            return query;
        }
    }


}
