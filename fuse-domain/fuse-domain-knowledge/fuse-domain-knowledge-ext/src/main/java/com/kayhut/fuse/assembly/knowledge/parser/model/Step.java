package com.kayhut.fuse.assembly.knowledge.parser.model;

import com.kayhut.fuse.model.execution.plan.Direction;

import java.util.Collections;
import java.util.Map;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Step {

    private String conceptId;
    private ElementType type;
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

    public ElementType getType() {
        return type;
    }

    public Step setType(ElementType type) {
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
