package com.kayhut.fuse.model.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Main reason why this class extends AbstractMap is for pretty json serialization when ExternalMetadata is empty
 * Using the NON_EMPTY jackson attribute include value will call the isEmpty() method which will cause this object not
 * to be serialized when it was constructed using the default ctor (Tried to achieve this with NON_DEFAULT as well but that didn't work)
 */
public class ExternalMetadata extends AbstractMap<String, String> {
    //region Constructors
    public ExternalMetadata() {

    }

    public ExternalMetadata(String id, String operation) {
        this.id = id;
        this.operation = operation;
    }
    //endregion

    //region AbstractMap Implementation
    @Override
    public Set<Entry<String, String>> entrySet() {
        return Stream.<Entry<String, String>>of(
                new AbstractMap.SimpleEntry<>("id", this.id),
                new AbstractMap.SimpleEntry<>("operation", this.operation))
                .filter(entry -> entry.getValue() != null)
                .toJavaSet();
    }
    //endregion

    //region Properties
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return String.format("{id: %s, operation: %s}", this.id, this.operation);
    }
    //endregion

    //region Fields
    private String id;
    private String operation;
    //endregion
}
