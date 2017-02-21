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
        return "ClassPojo [relationships = "+relationships+", entities = "+entities+"]";
    }

    //region Fields
    private List<Entity> entities;
    private List<Relationship> relationships;
    //endregion
}
