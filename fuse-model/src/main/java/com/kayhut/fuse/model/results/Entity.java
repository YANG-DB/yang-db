package com.kayhut.fuse.model.results;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */
@JsonPropertyOrder({ "eTag", "eID", "eType", "properties", "attachedProperties" })
public class Entity {

    public List<String> getETag ()
    {
        return eTag;
    }

    public void setETag (List<String> eTag)
    {
        this.eTag = eTag;
    }

    public List<AttachedProperty> getAttachedProperties ()
    {
        return attachedProperties;
    }

    public void setAttachedProperties (List<AttachedProperty> attachedProperties)
    {
        this.attachedProperties = attachedProperties;
    }

    public int getEType ()
    {
        return eType;
    }

    public void setEType (int eType)
    {
        this.eType = eType;
    }

    public String getEID ()
    {
        return eID;
    }

    public void setEID (String eID)
    {
        this.eID = eID;
    }

    public List<Property> getProperties ()
    {
        return properties;
    }

    public void setProperties (List<Property> properties)
    {
        this.properties = properties;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [eTag = "+eTag+", attachedProperties = "+attachedProperties+", eType = "+eType+", eID = "+eID+", properties = "+properties+"]";
    }

    //region Fields
    private List<String> eTag;
    private String eID;
    private int eType;
    private List<Property> properties;
    private List<AttachedProperty> attachedProperties;
    //endregion
}
