package com.kayhut.fuse.stat.model.configuration;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Mapping {

    //region Ctrs
    public Mapping() {
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
    public static final class MappingBuilder {
        private List<String> indices;
        private List<String> types;

        private MappingBuilder() {
        }

        public static MappingBuilder aMapping() {
            return new MappingBuilder();
        }

        public MappingBuilder withIndices(List<String> indices) {
            this.indices = indices;
            return this;
        }

        public MappingBuilder withTypes(List<String> types) {
            this.types = types;
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
