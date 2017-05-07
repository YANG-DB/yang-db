package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
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
    private List<String> report;
    private String units;
    //endregion

    //region Builder
    public static final class Builder {
        private int pType;
        private String name;
        private String type;
        private List<String> report;
        private String units;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withPType(int pType) {
            this.pType = pType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withReport(List<String> report) {
            this.report = report;
            return this;
        }

        public Builder withUnits(String units) {
            this.units = units;
            return this;
        }

        public Property build() {
            Property property = new Property();
            property.setName(name);
            property.setType(type);
            property.setReport(report);
            property.setUnits(units);
            property.pType = this.pType;
            return property;
        }
    }
    //endregion



}
