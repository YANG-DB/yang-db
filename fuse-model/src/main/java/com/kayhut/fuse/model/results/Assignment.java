package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Assignment {

    //region Properties
    public List<Relationship> getRelationships ()
    {
        return relationships;
    }

    public void setRelationships (List<Relationship> relationships)
    {
        this.relationships = relationships;
    }

    public List<Entity> getEntities ()
    {
        return entities;
    }

    public void setEntities (List<Entity> entities)
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
    private List<Entity> entities;
    private List<Relationship> relationships;
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
        public Builder withEntity(Entity entity) {
            Entity currentEntity = this.entities.get(entity.geteID());
            if (currentEntity != null) {
                entity = Entity.Builder.instance().withEntity(currentEntity).withEntity(entity).build();
            }

            entities.put(entity.geteID(), entity);
            return this;
        }

        public Builder withEntities(List<Entity> entities) {
            //entities.forEach(entity -> this.entities.put(entity.hashCode(), entity));
            entities.forEach(entity -> this.entities.put(entity.geteTag().get(0), entity));
            return this;
        }

        public Builder withRelationship(Relationship relationship) {
            this.relationships.add(relationship);
            return this;
        }

        public Builder withRelationships(List<Relationship> relationships) {
            this.relationships = relationships;
            return this;
        }

        public Assignment build() {
            Assignment assignment = new Assignment();
            //assignment.setEntities(Stream.ofAll(entities.values()).toJavaList());
            assignment.setEntities(Stream.ofAll(this.entities.values()).sortBy(entity -> entity.geteTag().get(0)).toJavaList());
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
