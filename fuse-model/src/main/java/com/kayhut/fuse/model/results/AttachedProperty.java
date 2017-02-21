package com.kayhut.fuse.model.results;

/**
 * Created by benishue on 21-Feb-17.
 */
public class AttachedProperty {

    public String getPName ()
    {
        return pName;
    }

    public void setPName (String pName)
    {
        this.pName = pName;
    }

    public Object getValue ()
    {
        return value;
    }

    public void setValue (Object value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [pName = "+pName+", value = "+value+"]";
    }

    //region Fields
    private String pName;
    private Object value;
    //endregion
}
