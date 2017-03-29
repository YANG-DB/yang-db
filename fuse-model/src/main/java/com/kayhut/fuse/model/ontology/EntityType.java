package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EntityType {

    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<String> getDisplay() {
        return display;
    }

    public void setDisplay(List<String> display) {
        this.display = display;
    }

    public String getDBeName() {
        return DBeName;
    }

    public void setDBeName(String DBeName) {
        this.DBeName = DBeName;
    }

    @Override
    public String toString()
    {
        return "EntityType [eType = "+eType+", name = "+name+", display = "+display+", properties = "+properties+"]";
    }

    //region Fields
    private int eType;
    private String DBeName;
    private String name;
    private List<Property> properties;
    private List<String> display;
    //endregion

    //region Builder
    public static final class EntityTypeBuilder {
        private int eType;
        private String DBeName;
        private String name;
        private List<Property> properties;
        private List<String> display;

        private EntityTypeBuilder() {
        }

        public static EntityTypeBuilder anEntityType() {
            return new EntityTypeBuilder();
        }

        public EntityTypeBuilder withEType(int eType) {
            this.eType = eType;
            return this;
        }

        public EntityTypeBuilder withDBeName(String DBeName) {
            this.DBeName = DBeName;
            return this;
        }

        public EntityTypeBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EntityTypeBuilder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public EntityTypeBuilder withDisplay(List<String> display) {
            this.display = display;
            return this;
        }

        public EntityType build() {
            EntityType entityType = new EntityType();
            entityType.setDBeName(DBeName);
            entityType.setName(name);
            entityType.setProperties(properties);
            entityType.setDisplay(display);
            entityType.eType = this.eType;
            return entityType;
        }
    }
    //endregion

}
