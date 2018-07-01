package com.kayhut.fuse.stat.model.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Mapping {

    //region Ctrs
    public Mapping() {
        //needed for Jackson
    }

    public Mapping(List<String> indices, List<String> types) {
        this.indices = indices;
        this.types = types;
    }
    //endregion

    //region Getters & Setters
    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
    //endregion

    //region Fields
    private List<String> indices;
    private List<String> types;
    //endregion

    //region Builder
    public static final class Builder {
        private List<String> indices;
        private List<String> types;

        private Builder() {
            this.indices = new ArrayList<>();
            this.types = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withIndices(List<String> indices) {
            this.indices = indices;
            return this;
        }

        public Builder withIndex(String index) {
            this.indices.add(index);
            return this;
        }

        public Builder withTypes(List<String> types) {
            this.types = types;
            return this;
        }

        public Builder withType(String type) {
            this.types.add(type);
            return this;
        }

        public Mapping build() {
            Mapping mapping = new Mapping();
            mapping.setIndices(indices);
            mapping.setTypes(types);
            return mapping;
        }
    }
    //endregion

}
