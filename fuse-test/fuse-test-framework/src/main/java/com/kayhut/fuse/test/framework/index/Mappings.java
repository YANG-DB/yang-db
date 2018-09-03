package com.kayhut.fuse.test.framework.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 5/16/2017.
 */
public class Mappings {
    public static class Mapping {
        public static class Property {
            public enum Type {
                integer,
                @JsonProperty("long")
                long_,
                @JsonProperty("double")
                double_,
                string,
                text,
                keyword,
                date
            }

            public enum Index {
                analyzed,
                not_analyzed
            }

            //region Constructors
            public Property() {
                this.properties = new HashMap<>();
            }

            public Property(Type type) {
                this();
                this.type = type;
            }

            public Property(Type type, Index index) {
                this(type);
                this.index = index;
            }

            public Property(Type type, String format) {
                this(type);
                this.format = format;
            }
            //endregion

            //region Public Methods
            public Property addProperty(String name, Property property) {
                this.properties.put(name, property);
                return this;
            }
            //endregion

            //region Properties
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public Type getType() {
                return type;
            }

            @JsonInclude(JsonInclude.Include.NON_NULL)
            public Index getIndex() {
                return index;
            }

            @JsonInclude(JsonInclude.Include.NON_NULL)
            public String getFormat() {
                return this.format;
            }

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public Map<String, Property> getProperties() {
                return this.properties;
            }
            //endregion

            //region Fields
            private Type type;
            private Index index;
            private String format;
            private Map<String, Property> properties;
            //endregion
        }

        //region Constructors
        public Mapping() {
            this.properties = new HashMap<>();
        }
        //endregion

        //region Public Methods
        public Mapping addProperty(String name, Property property) {
            this.properties.put(name, property);
            return this;
        }
        //endregion

        //region Properties
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Map<String, Property> getProperties() {
            return this.properties;
        }
        //endregion

        //region Fields
        private Map<String, Property> properties;
        //endregion
    }

    //region Constructors
    public Mappings() {
        this.mappings = new HashMap<>();
    }
    //endregion

    //region Public Methods
    public Mappings addMapping(String name, Mapping mapping) {
        this.mappings.put(name, mapping);
        return this;
    }
    //endregion

    //region Properties
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Mapping> getMappings() {
        return this.mappings;
    }
    //endregion

    //region Fields
    private Map<String, Mapping> mappings;
    //endregion
}
