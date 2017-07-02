package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by benishue on 21-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Entity {
    //region Constructors
    public Entity() {
        this.properties = Collections.emptyList();
        this.attachedProperties = Collections.emptyList();
    }
    //endregion

    //region Properties
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

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
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
    //endregion

    //region Override Methods
    @Override
    public int hashCode() {
        int hashCode = eID.hashCode() * 31;
        hashCode = hashCode * 31 + eType.hashCode();
        hashCode = hashCode * 31 + eTag.hashCode();
        return hashCode;
    }

    @Override
    public String toString()
    {
        return "Entity [eTag = " + eTag + ", attachedProperties = " + attachedProperties + ", eType = " + eType + ", eID = "+eID+", properties = " + properties + "]";
    }
    //endregion

    //region Fields
    private List<String> eTag;
    private String eID;
    private String eType;
    private List<Property> properties;
    private List<AttachedProperty> attachedProperties;
    //endregion

    //region Builder
    public static final class Builder {
        //region Constructors
        private Builder() {
            this.eTag = Collections.emptyList();
            this.properties = Collections.emptyList();
            this.attachedProperties = Collections.emptyList();
            this.entities = Collections.emptyList();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withETag(List<String> eTag) {
            this.eTag = eTag;
            return this;
        }

        public Builder withEID(String eID) {
            this.eID = eID;
            return this;
        }

        public Builder withEType(String eType) {
            this.eType = eType;
            return this;
        }

        public Builder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public Builder withAttachedProperties(List<AttachedProperty> attachedProperties) {
            this.attachedProperties = attachedProperties;
            return this;
        }

        public Builder withEntity(Entity entity) {
            if (this.entities.isEmpty()) {
                this.entities = new ArrayList<>();
            }

            this.entities.add(entity);
            return this;
        }

        public Entity build() {
            Entity entity = new Entity();
            entity.setProperties(properties);
            entity.setAttachedProperties(attachedProperties);
            entity.eType = this.eType;
            entity.eID = this.eID;
            entity.eTag = this.eTag;

            for(Entity entityToMerge : this.entities) {
                entity = merge(entity, entityToMerge);
            }


            return entity;
        }
        //endregion

        //region Private Methods
        private Entity merge(Entity e1, Entity e2) {
            e1.seteTag(Stream.ofAll(e1.geteTag())
                    .appendAll(e2.geteTag())
                    .distinct()
                    .toJavaList());

            e1.setProperties(Stream.ofAll(e1.getProperties())
                    .appendAll(e2.getProperties())
                    .distinctBy(Property::getpType)
                    .toJavaList());

            e1.setAttachedProperties(Stream.ofAll(e1.getAttachedProperties())
                .appendAll(e2.getAttachedProperties())
                .distinctBy(AttachedProperty::getpName)
                .toJavaList());

            return e1;
        }
        //endregion

        //region Fields
        private List<String> eTag;
        private String eID;
        private String eType;
        private List<Property> properties;
        private List<AttachedProperty> attachedProperties;
        private List<Entity> entities;
        //endregion
    }


}
