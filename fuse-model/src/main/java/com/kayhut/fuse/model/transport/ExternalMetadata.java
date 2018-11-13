package com.kayhut.fuse.model.transport;

/*-
 * #%L
 * ExternalMetadata.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
