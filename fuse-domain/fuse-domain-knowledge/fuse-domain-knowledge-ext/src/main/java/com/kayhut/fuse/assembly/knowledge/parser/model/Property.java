package com.kayhut.fuse.assembly.knowledge.parser.model;



public class Property {

    private String matchType;
    private Object value;

    public Property() {
    }

    public Property(String matchType, Object value) {
        this.matchType = matchType;
        this.value = value;
    }

    public String getMatchType() {
        return matchType;
    }

    public Object getValue() {
        return value;
    }
}
