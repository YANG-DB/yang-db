package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty("DBrName")
    public String getDBrName() {
        return DBrName;
    }

    @JsonProperty("DBrName")
    public void setDBrName(String DBrName) {
        this.DBrName = DBrName;
    }

    public List<EPair> getePairs() {
        return ePairs;
    }

    public void setePairs(List<EPair> ePairs) {
        this.ePairs = ePairs;
    }

    public List<Integer> getProperties() {
        return properties;
    }

    public void setProperties(List<Integer> properties) {
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
    private String DBrName;
    private List<EPair> ePairs;
    private List<Integer> properties;
    //endregion

    //region Builder
    public static final class Builder {
        private int rType;
        private String name;
        private boolean directional;
        private String DBrName;
        private List<EPair> ePairs;
        private List<Integer> properties;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withRType(int rType) {
            this.rType = rType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDirectional(boolean directional) {
            this.directional = directional;
            return this;
        }

        public Builder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        public Builder withEPairs(List<EPair> ePairs) {
            this.ePairs = ePairs;
            return this;
        }

        public Builder withProperties(List<Integer> properties) {
            this.properties = properties;
            return this;
        }

        public RelationshipType build() {
            RelationshipType relationshipType = new RelationshipType();
            relationshipType.setName(name);
            relationshipType.setDirectional(directional);
            relationshipType.setDBrName(DBrName);
            relationshipType.setProperties(properties);
            relationshipType.ePairs = this.ePairs;
            relationshipType.rType = this.rType;
            return relationshipType;
        }
    }
    //endregion



}
