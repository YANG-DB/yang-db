package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EntityType {
    public EntityType() {
    }

    public EntityType(String type, String name, List<String> properties) {
        this.eType = type;
        this.name = name;
        this.properties = properties;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public List<String> getDisplay() {
        return display;
    }

    public void setDisplay(List<String> display) {
        this.display = display;
    }

    @Override
    public String toString()
    {
        return "EntityType [eType = "+eType+", name = "+name+", display = "+display+", properties = "+properties+"]";
    }

    //region Fields
    private String eType;
    private String name;
    private List<String> properties;
    private List<String> display;
    //endregion

    //region Builder
    public static final class Builder {
        private String eType;
        private String name;
        private List<String> properties;
        private List<String> display;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withEType(String eType) {
            this.eType = eType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        public Builder withDisplay(List<String> display) {
            this.display = display;
            return this;
        }

        public EntityType build() {
            EntityType entityType = new EntityType();
            entityType.setName(name);
            entityType.setProperties(properties);
            entityType.setDisplay(display);
            entityType.eType = this.eType;
            return entityType;
        }
    }
    //endregion

}
