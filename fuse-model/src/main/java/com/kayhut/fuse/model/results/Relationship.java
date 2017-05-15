package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Relationship {
    //region Properties
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

    public String geteTag1() {
        return eTag1;
    }

    public void seteTag1(String eTag1) {
        this.eTag1 = eTag1;
    }

    public String geteTag2() {
        return eTag2;
    }

    public void seteTag2(String eTag2) {
        this.eTag2 = eTag2;
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
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "Relationship [eID1 = "+eID1+", rType = "+rType+", attachedProperties = "+attachedProperties+", eID2 = "+eID2+", directional = "+directional+", agg = "+agg+", properties = "+properties+", rID = "+rID+"]";
    }
    //endregion

    //region Fields
    private String rID;
    private boolean agg;
    private int rType;
    private boolean directional;
    private String eID1;
    private String eID2;
    private String eTag1;
    private String eTag2;
    private List<Property> properties;
    private List<AttachedProperty> attachedProperties;
    //endregion


    public static final class Builder {
        //region Constructors
        private Builder() {
        }
        //endregion

        //region Static
        public static Builder aRelationship() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withRID(String rID) {
            this.rID = rID;
            return this;
        }

        public Builder withAgg(boolean agg) {
            this.agg = agg;
            return this;
        }

        public Builder withRType(int rType) {
            this.rType = rType;
            return this;
        }

        public Builder withDirectional(boolean directional) {
            this.directional = directional;
            return this;
        }

        public Builder withEID1(String eID1) {
            this.eID1 = eID1;
            return this;
        }

        public Builder withEID2(String eID2) {
            this.eID2 = eID2;
            return this;
        }

        public Builder withETag1(String eTag1) {
            this.eTag1 = eTag1;
            return this;
        }

        public Builder withETag2(String eTag2) {
            this.eTag2 = eTag2;
            return this;
        }

        public Builder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public Builder withAttachedProperties(List<AttachedProperty> attachedProperties) {
            this.attachedProperties = attachedProperties;
            return this;
        }

        public Relationship build() {
            Relationship relationship = new Relationship();
            relationship.setAgg(agg);
            relationship.setDirectional(directional);
            relationship.setProperties(properties);
            relationship.setAttachedProperties(attachedProperties);
            relationship.eID1 = this.eID1;
            relationship.rID = this.rID;
            relationship.rType = this.rType;
            relationship.eID2 = this.eID2;
            relationship.eTag1 = this.eTag1;
            relationship.eTag2 = this.eTag2;
            return relationship;
        }
        //endregion

        //region Fields
        private String rID;
        private boolean agg;
        private int rType;
        private boolean directional;
        private String eID1;
        private String eID2;
        private String eTag1;
        private String eTag2;
        private List<Property> properties;
        private List<AttachedProperty> attachedProperties;
        //endregion
    }


}
