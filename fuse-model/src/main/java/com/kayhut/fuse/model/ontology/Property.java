package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Property {

    public int getpType() {
        return pType;
    }

    public void setpType(int pType) {
        this.pType = pType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getReport() {
        return report;
    }

    public void setReport(List<String> report) {
        this.report = report;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    @JsonProperty("DBpName")
    public String getDBpName() {
        return DBpName;
    }

    @JsonProperty("DBpName")
    public void setDBpName(String DBpName) {
        this.DBpName = DBpName;
    }

    @Override
    public String toString()
    {
        return "Property [name = "+name+", report = "+report+", type = "+type+", pType = "+pType+"]";
    }

    //region Fields
    private int pType;
    private String name;
    private String type;
    private String DBpName;
    private List<String> report;
    private String height;
    private String units;
    //endregion

    //region Builder
    public static final class PropertyBuilder {
        private int pType;
        private String name;
        private String type;
        private String DBrName;
        private List<String> report;
        private String height;
        private String units;

        private PropertyBuilder() {
        }

        public static PropertyBuilder aProperty() {
            return new PropertyBuilder();
        }

        public PropertyBuilder withPType(int pType) {
            this.pType = pType;
            return this;
        }

        public PropertyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public PropertyBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public PropertyBuilder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        public PropertyBuilder withReport(List<String> report) {
            this.report = report;
            return this;
        }

        public PropertyBuilder withHeight(String height) {
            this.height = height;
            return this;
        }

        public PropertyBuilder withUnits(String units) {
            this.units = units;
            return this;
        }

        public Property build() {
            Property property = new Property();
            property.setName(name);
            property.setType(type);
            property.setDBpName(DBrName);
            property.setReport(report);
            property.setHeight(height);
            property.setUnits(units);
            property.pType = this.pType;
            return property;
        }
    }
    //endregion



}
