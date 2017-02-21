package com.kayhut.fuse.model.results;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */
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
