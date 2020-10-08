package com.yangdb.fuse.model.results;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


/*-
 *
 * Entity.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.model.logical.Vertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 21-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity implements Vertex<Entity> {
    //region Constructors
    public Entity() {
        this.properties = new HashMap<>();
        this.attachedProperties = Collections.emptyList();
    }

    public Entity(Set<String> eTag, String eID, String eType, Map<String, Property> properties) {
        this.eTag = eTag;
        this.eID = eID;
        this.eType = eType;
        this.properties = properties;
    }
    //endregion

    //region Properties

    @Override
    @JsonProperty("id")
    public String id() {
        return geteID();
    }

    @JsonProperty("id")
    public void seteID(String eID) {
        this.eID = eID;
    }

    @Override
    @JsonProperty("label")
    public String label() {
        return geteType();
    }

    @Override
    @JsonProperty("label")
    public Entity label(String label) {
        seteType(label);
        return this;
    }

    @Override
    @JsonProperty("tag")
    public String tag() {
        return geteTag().iterator().next();
    }

    @Override
    @JsonProperty("tag")
    public Entity tag(String tag) {
        seteTag(Collections.singleton(tag));
        return this;
    }

    @Override
    public Entity merge(Entity entity) {
        return Builder.instance().merge(this,entity);
    }

    public Set<String> geteTag() {
        return eTag;
    }

    public void seteTag(Set<String> eTag) {
        this.eTag = eTag;
    }

    public String geteID() {
        return eID;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public Optional<Property> getProperty(String key) {
        return getProperties().stream().filter(p -> p.getpType().equals(key)).findAny();
    }

    public Collection<Property> getProperties() {
        return properties.values();
    }

    public void setProperties(Collection<Property> properties) {
        this.properties = Stream.ofAll(properties)
                .toJavaMap(property -> new Tuple2<>(property.getpType(), property));
    }

    public void setProperty(Property property) {
        this.properties.put(property.getpType(), property);
    }

    public List<AttachedProperty> getAttachedProperties() {
        return attachedProperties;
    }

    public void setAttachedProperties(List<AttachedProperty> attachedProperties) {
        this.attachedProperties = attachedProperties;
    }

    @Override
    @JsonIgnore()
    public Map<String, Object> metadata() {
        return getProperties().stream().collect(
                Collectors.toMap(Property::getpType, Property::getValue));
    }

    @Override
    @JsonIgnore()
    public Map<String, Object> fields() {
        return getAttachedProperties().stream().collect(
                Collectors.toMap(AttachedProperty::getpName, AttachedProperty::getValue));

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
    public String toString() {
        return "Entity [eTag = " + eTag + ", attachedProperties = " + attachedProperties + ", eType = " + eType + ", eID = " + eID + ", properties = " + properties + "]";
    }
    //endregion

    //region Fields
    private Set<String> eTag;
    private String eID;
    private String eType;
    private Map<String, Property> properties;
    private List<AttachedProperty> attachedProperties;

    //endregion

    //region Builder
    public static class Builder {
        //region Constructors
        protected Builder() {
            this.eTag = new HashSet<>();
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
        public Builder withETag(Set<String> eTag) {
            this.eTag = eTag;
            return this;
        }

        public Builder withETag(String eTag) {
            this.eTag = Collections.singleton(eTag);
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

        protected Entity _build(Entity entity) {
            entity.setProperties(properties);
            entity.setAttachedProperties(attachedProperties);
            entity.eType = this.eType;
            entity.eID = this.eID;
            entity.eTag = this.eTag;

            for (Entity entityToMerge : this.entities) {
                entity = merge(entity, entityToMerge);
            }

            return entity;
        }

        public Entity build() {
            return _build(new Entity());
        }
        //endregion

        //region Private Methods
        public Entity merge(Entity e1, Entity e2) {
            e1.seteID(e1.geteID() == null ? e2.geteID() : e1.geteID());
            e1.seteType(e1.geteType() == null ? e2.geteType() : e1.geteType());
            e1.eTag.addAll(e2.eTag);
            e1.properties.putAll(e2.properties);

            return e1;
        }
        //endregion

        //region Fields
        private Set<String> eTag;
        private String eID;
        private String eType;
        private List<Property> properties;
        private List<AttachedProperty> attachedProperties;
        private List<Entity> entities;
        //endregion
    }


}
