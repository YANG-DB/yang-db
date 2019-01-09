package com.kayhut.fuse.assembly.knowledge.parser.model;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import javaslang.collection.Stream;
import javaslang.control.Option;

public enum Types {
    //metadata
    CATEGORY("http://huha.com#category","stringValue"),
    CONTEXT("http://huha.com#context","stringValue"),

    //business types
    PHONE("http://huha.com#phone","stringValue"),
    TITLE("http://huha.com#title","stringValue"),
    BIRTHDAY("http://huha.com/minimal#birthday","dateValue"),
    DURATION("http://huha.com#duration","intValue"),
    STRING("http://huha.com#stringValue","stringValue");

    private String value;
    private String fieldType;

    Types(String value, String fieldType) {
        this.value = value;
        this.fieldType = fieldType;
    }

    public static Types byValue(String value) {
        final Option<Types> type = Stream.of(Types.values()).find(v -> v.getValue().equals(value));
        if(type.isEmpty())
            return STRING;

        return type.get();
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getValue() {
        return value;
    }

    public String getSuffix() {
        return getValue().split("#")[1];
    }
}
