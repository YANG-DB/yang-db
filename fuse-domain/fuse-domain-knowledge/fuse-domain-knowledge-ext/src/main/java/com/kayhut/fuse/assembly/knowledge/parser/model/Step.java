package com.kayhut.fuse.assembly.knowledge.parser.model;

import java.util.Collections;
import java.util.Map;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Step {

    private String conceptId;
    private ElemType type;
    private Direction direction;
//    @JsonIgnoreProperties(ignoreUnknown = true)
    private Map<String, Property> properties = Collections.emptyMap();


    public String getConceptId() {
        return conceptId;
    }

    public Step setConceptId(String conceptId) {
        this.conceptId = conceptId;
        return this;
    }

    public ElemType getType() {
        return type;
    }

    public Step setType(ElemType type) {
        this.type = type;
        return this;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public Step setProperties(Map<String, Property> properties) {
        this.properties = properties;
        return this;
    }

    public Direction getDirection() {
        return direction;
    }
}
