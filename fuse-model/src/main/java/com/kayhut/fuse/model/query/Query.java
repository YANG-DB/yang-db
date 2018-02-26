package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.kayhut.fuse.model.asgQuery.IQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by user on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder=Query.Builder.class)
public class Query implements IQuery<EBase>{

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

        public Builder withNonidentical(List<List<String>> nonidentical) {
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
    }
}
