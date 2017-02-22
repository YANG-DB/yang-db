package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Relationship {

    public String getrID() {
        return rID;
    }

    public void setrID(String rID) {
        this.rID = rID;
    }

    public boolean isAgg() {
        return agg;
    }

    public void setAgg(boolean agg) {
        this.agg = agg;
    }

    public int getrType() {
        return rType;
    }

    public void setrType(int rType) {
        this.rType = rType;
    }

    public boolean isDirectional() {
        return directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
    }

    public String geteID1() {
        return eID1;
    }

    public void seteID1(String eID1) {
        this.eID1 = eID1;
    }

    public String geteID2() {
        return eID2;
    }

    public void seteID2(String eID2) {
        this.eID2 = eID2;
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
        return "Relationship [eID1 = "+eID1+", rType = "+rType+", attachedProperties = "+attachedProperties+", eID2 = "+eID2+", directional = "+directional+", agg = "+agg+", properties = "+properties+", rID = "+rID+"]";
    }

    //region Fields
    private String rID;
    private boolean agg;
    private int rType;
    private boolean directional;
    private String eID1;
    private String eID2;
    private List<Property> properties;
    private List<AttachedProperty> attachedProperties;
    //endregion
}
