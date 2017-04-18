package com.kayhut.fuse.model.ontology;

/**
 * Created by moti on 4/18/2017.
 */
public class PrimitiveType {
    private String type;
    private Class javaType;

    public PrimitiveType(String type, Class javaType) {
        this.type = type;
        this.javaType = javaType;
    }

    public String getType() {
        return type;
    }

    public Class getJavaType() {
        return javaType;
    }
}
