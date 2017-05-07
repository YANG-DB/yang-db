package com.kayhut.fuse.stat.model.configuration;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatContainer {

    //region Getters & Setters
    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }
    //endregion

    //region Fields
    private List<Mapping> mappings;
    private List<Type> types;
    //endregion

    //region Builder
    public static final class StatContainerBuilder {
        private List<Mapping> mappings;
        private List<Type> types;

        private StatContainerBuilder() {
        }

        public static StatContainerBuilder aStatContainer() {
            return new StatContainerBuilder();
        }

        public StatContainerBuilder withMappings(List<Mapping> mappings) {
            this.mappings = mappings;
            return this;
        }

        public StatContainerBuilder withTypes(List<Type> types) {
            this.types = types;
            return this;
        }

        public StatContainer build() {
            StatContainer statContainer = new StatContainer();
            statContainer.setMappings(mappings);
            statContainer.setTypes(types);
            return statContainer;
        }
    }
    //endregion

}
