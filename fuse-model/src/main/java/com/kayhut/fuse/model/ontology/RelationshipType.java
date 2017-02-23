package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelationshipType {

    public int getrType() {
        return rType;
    }

    public void setrType(int rType) {
        this.rType = rType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectional() {
        return directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
    }

    public List<EPair> getePairs() {
        return ePairs;
    }

    public void setePairs(List<EPair> ePairs) {
        this.ePairs = ePairs;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public String toString()
    {
        return "RelationshipType [ePairs = "+ePairs+", rType = "+rType+", directional = "+directional+", name = "+name+", properties = "+properties+"]";
    }

    //region Fields
    private int rType;
    private String name;
    private boolean directional;
    private List<EPair> ePairs;
    private List<Property> properties;
    //endregion
}
