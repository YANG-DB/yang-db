package com.kayhut.fuse.model.results;

/*-
 * #%L
 * Assignment.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment<E,R> {
    //region Constructors
    public Assignment() {
        this.entities = Collections.emptyList();
        this.relationships = Collections.emptyList();
    }
    //endregion

    //region Properties
    public List<R> getRelationships ()
    {
        return relationships;
    }

    public void setRelationships (List<R> relationships)
    {
        this.relationships = relationships;
    }

    public List<E> getEntities ()
    {
        return entities;
    }

    public void setEntities (List<E> entities)
    {
        this.entities = entities;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "Assignment [relationships = " + relationships + ", entities = " + entities + "]";
    }
    //endregion

    //region Fields
    private List<E> entities;
    private List<R> relationships;
    //endregion

    public static final class Builder {
        //region Constructors
        private Builder() {
            //entities = new HashMap<>();
            entities = new HashMap<>();
            relationships = new ArrayList<>();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withEntities(List<Entity> entities) {
            entities.forEach(this::withEntity);
            return this;
        }

        public Builder withEntity(Entity entity) {
            Entity currentEntity = this.entities.get(entity.geteID());
            if (currentEntity != null) {
                entity = Entity.Builder.instance().withEntity(currentEntity).withEntity(entity).build();
            }

            entities.put(entity.geteID(), entity);
            return this;
        }

        public Builder withEntity(Entity entity,String tag) {
            Entity currentEntity = this.entities.get(entity.geteID());
            if (currentEntity != null) {
                entity = Entity.Builder.instance().withEntity(currentEntity).withEntity(entity).withETag(tag).build();
            }

            entities.put(entity.geteID(), entity);
            return this;
        }

        public Builder withRelationship(Relationship relationship) {
            this.relationships.add(relationship);
            return this;
        }

        public Builder withRelationships(List<Relationship> relationships) {
            this.relationships.addAll(relationships);
            return this;
        }

        public Assignment<Entity,Relationship> build() {
            Assignment<Entity,Relationship> assignment = new Assignment<>();
            //assignment.setEntities(Stream.ofAll(entities.values()).toJavaList());
            assignment.setEntities(Stream.ofAll(this.entities.values()).sortBy(Entity::geteType).toJavaList());
            assignment.setRelationships(this.relationships);
            return assignment;
        }
        //endregion

        //region Fields
        private Map<String, Entity> entities;
        private List<Relationship> relationships;

        //endregion
    }



}
