package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Assignment extends QueryResult {

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

    @Override
    public String toString()
    {
        return "Assignment [relationships = "+relationships+", entities = "+entities+"]";
    }

    //region Fields
    private List<Entity> entities;
    private List<Relationship> relationships;
    //endregion

    public static final class AssignmentBuilder {
        //region Constructors
        private AssignmentBuilder() {
            entities = new HashMap<>();
            relationships = new ArrayList<>();
        }
        //endregion

        //region Static
        public static AssignmentBuilder anAssignment() {
            return new AssignmentBuilder();
        }
        //endregion

        //region Public Methods
        public AssignmentBuilder withEntity(Entity entity) {
            Entity entityToMerge = this.entities.get(entity.hashCode());
            if (entityToMerge != null) {
                entity = Entity.Builder.anEntity().withEntity(entity).withEntity(entityToMerge).build();
            }

            entities.put(entity.hashCode(), entity);
            return this;
        }

        public AssignmentBuilder withEntities(List<Entity> entities) {
            entities.forEach(entity -> this.entities.put(entity.hashCode(), entity));
            return this;
        }

        public AssignmentBuilder withRelationship(Relationship relationship) {
            this.relationships.add(relationship);
            return this;
        }

        public AssignmentBuilder withRelationships(List<Relationship> relationships) {
            this.relationships = relationships;
            return this;
        }

        public Assignment build() {
            Assignment assignment = new Assignment();
            assignment.setEntities(Stream.ofAll(entities.values()).toJavaList());
            assignment.setRelationships(relationships);
            return assignment;
        }
        //endregion

        //region Fields
        private Map<Integer, Entity> entities;
        private List<Relationship> relationships;
        //endregion
    }



}
