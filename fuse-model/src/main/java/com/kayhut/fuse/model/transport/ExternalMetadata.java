package com.kayhut.fuse.model.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Main reason why this class extends AbstractMap is for pretty json serialization when ExternalMetadata is empty
 * Using the NON_EMPTY jackson attribute include value will call the isEmpty() method which will cause this object not
 * to be serialized when it was constructed using the default ctor (Tried to achieve this with NON_DEFAULT as well but that didn't work)
 */
public class ExternalMetadata extends HashMap<String, String> {
    //region Constructors
    public ExternalMetadata() {

    }

    public ExternalMetadata(String id, String operation) {
        this.setId(id);
        this.setOperation(operation);
    }
    //endregion

    //region Properties
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getId() {
        return this.get("id");
    }

    public void setId(String id) {
        if (id != null) {
            this.put("id", id);
        } else {
            this.remove("id");
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getOperation() {
        return this.get("operation");
    }

    public void setOperation(String operation) {
        if (operation != null) {
            this.put("operation", operation);
        } else {
            this.remove("operation");
        }
    }
    //endregion
}
