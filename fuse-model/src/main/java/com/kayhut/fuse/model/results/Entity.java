package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Entity {

    public List<String> geteTag() {
        return eTag;
    }

    public void seteTag(List<String> eTag) {
        this.eTag = eTag;
    }

    public String geteID() {
        return eID;
    }

    public void seteID(String eID) {
        this.eID = eID;
    }

    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<AttachedProperty> getAttachedProperties() {
        return attachedProperties;
    }

    public void setAttachedProperties(List<AttachedProperty> attachedProperties) {
        this.attachedProperties = attachedProperties;
    }

    @Override
    public String toString()
    {
        return "Entity [eTag = "+eTag+", attachedProperties = "+attachedProperties+", eType = "+eType+", eID = "+eID+", properties = "+properties+"]";
    }

    //region Fields
    private List<String> eTag;
    private String eID;
    private int eType;
    private List<Property> properties;
    private List<AttachedProperty> attachedProperties;
    //endregion

    public static final class EntityBuilder {
        private List<String> eTag;
        private String eID;
        private int eType;
        private List<Property> properties;
        private List<AttachedProperty> attachedProperties;

        private EntityBuilder() {
        }

        public static EntityBuilder anEntity() {
            return new EntityBuilder();
        }

        public EntityBuilder withETag(List<String> eTag) {
            this.eTag = eTag;
            return this;
        }

        public EntityBuilder withEID(String eID) {
            this.eID = eID;
            return this;
        }

        public EntityBuilder withEType(int eType) {
            this.eType = eType;
            return this;
        }

        public EntityBuilder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public EntityBuilder withAttachedProperties(List<AttachedProperty> attachedProperties) {
            this.attachedProperties = attachedProperties;
            return this;
        }

        public Entity build() {
            Entity entity = new Entity();
            entity.setProperties(properties);
            entity.setAttachedProperties(attachedProperties);
            entity.eType = this.eType;
            entity.eID = this.eID;
            entity.eTag = this.eTag;
            return entity;
        }
    }


}
