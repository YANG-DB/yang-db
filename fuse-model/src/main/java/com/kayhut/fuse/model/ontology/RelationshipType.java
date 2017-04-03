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
    private String DBrName;
    private List<EPair> ePairs;
    private List<Property> properties;
    //endregion

    //region Builder
    public static final class RelationshipTypeBuilder {
        private int rType;
        private String name;
        private boolean directional;
        private String DBrName;
        private List<EPair> ePairs;
        private List<Property> properties;

        private RelationshipTypeBuilder() {
        }

        public static RelationshipTypeBuilder aRelationshipType() {
            return new RelationshipTypeBuilder();
        }

        public RelationshipTypeBuilder withRType(int rType) {
            this.rType = rType;
            return this;
        }

        public RelationshipTypeBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RelationshipTypeBuilder withDirectional(boolean directional) {
            this.directional = directional;
            return this;
        }

        public RelationshipTypeBuilder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        public RelationshipTypeBuilder withEPairs(List<EPair> ePairs) {
            this.ePairs = ePairs;
            return this;
        }

        public RelationshipTypeBuilder withProperties(List<Property> properties) {
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
