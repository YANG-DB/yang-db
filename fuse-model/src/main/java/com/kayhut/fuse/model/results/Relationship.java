package com.kayhut.fuse.model.results;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */
public class Relationship {

    public String getEID1 ()
    {
        return eID1;
    }

    public void setEID1 (String eID1)
    {
        this.eID1 = eID1;
    }

    public int getRType ()
    {
        return rType;
    }

    public void setRType (int rType)
    {
        this.rType = rType;
    }

    public List<AttachedProperty> getAttachedProperties ()
    {
        return attachedProperties;
    }

    public void setAttachedProperties (List<AttachedProperty> attachedProperties)
    {
        this.attachedProperties = attachedProperties;
    }

    public String getEID2 ()
    {
        return eID2;
    }

    public void setEID2 (String eID2)
    {
        this.eID2 = eID2;
    }

    public boolean getDirectional ()
    {
        return directional;
    }

    public void setDirectional (boolean directional)
    {
        this.directional = directional;
    }

    public boolean getAgg ()
    {
        return agg;
    }

    public void setAgg (boolean agg)
    {
        this.agg = agg;
    }

    public List<Property> getProperties ()
    {
        return properties;
    }

    public void setProperties (List<Property> properties)
    {
        this.properties = properties;
    }

    public String getRID ()
    {
        return rID;
    }

    public void setRID (String rID)
    {
        this.rID = rID;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [eID1 = "+eID1+", rType = "+rType+", attachedProperties = "+attachedProperties+", eID2 = "+eID2+", directional = "+directional+", agg = "+agg+", properties = "+properties+", rID = "+rID+"]";
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
