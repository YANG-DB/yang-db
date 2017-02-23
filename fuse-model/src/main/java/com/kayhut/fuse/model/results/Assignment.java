package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Assignment {

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
        private List<Entity> entities;
        private List<Relationship> relationships;

        private AssignmentBuilder() {
        }

        public static AssignmentBuilder anAssignment() {
            return new AssignmentBuilder();
        }

        public AssignmentBuilder withEntities(List<Entity> entities) {
            this.entities = entities;
            return this;
        }

        public AssignmentBuilder withRelationships(List<Relationship> relationships) {
            this.relationships = relationships;
            return this;
        }

        public Assignment build() {
            Assignment assignment = new Assignment();
            assignment.setEntities(entities);
            assignment.setRelationships(relationships);
            return assignment;
        }
    }



}
