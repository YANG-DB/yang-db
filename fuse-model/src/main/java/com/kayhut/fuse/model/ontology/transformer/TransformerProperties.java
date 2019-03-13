package com.kayhut.fuse.model.ontology.transformer;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransformerProperties {
    private String pattern;
    private String concreteType;
    private List<Map<String,String>> keys;
    private List<Map<String,String>> valuePatterns;

    public TransformerProperties() {}

    public TransformerProperties(String pattern, String eType, List<Map<String,String>> keys, List<Map<String,String>> valuePatterns) {
        this.pattern = pattern;
        this.concreteType = eType;
        this.keys = keys;
        this.valuePatterns = valuePatterns;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getConcreteType() {
        return concreteType;
    }

    public void setConcreteType(String concreteType) {
        this.concreteType = concreteType;
    }

    public List<Map<String,String>> getKeys() {
        return keys;
    }

    public void setKeys(List<Map<String,String>> keys) {
        this.keys = keys;
    }

    public List<Map<String,String>> getValuePatterns() {
        return valuePatterns;
    }

    public void setValuePatterns(List<Map<String,String>> valuePatterns) {
        this.valuePatterns = valuePatterns;
    }
}
