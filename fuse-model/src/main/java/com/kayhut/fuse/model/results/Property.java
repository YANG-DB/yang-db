package com.kayhut.fuse.model.results;

/**
 * Created by benishue on 21-Feb-17.
 */
public class Property {

    public String getAgg ()
    {
        return agg;
    }

    public void setAgg (String agg)
    {
        this.agg = agg;
    }

    public Object getValue ()
    {
        return value;
    }

    public void setValue (Object value)
    {
        this.value = value;
    }

    public int getPType ()
    {
        return pType;
    }

    public void setPType (int pType)
    {
        this.pType = pType;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [agg = "+agg+", value = "+value+", pType = "+pType+"]";
    }

    //region Fields
    private int pType;
    private String agg;
    private Object value;
    //endregion

}
