package com.kayhut.fuse.assembly.knowledge.parser.model;

import javaslang.collection.Stream;
import javaslang.control.Option;

public enum Types {
    TITLE("http://huha.com#title","stringValue"),
    BIRTHDAY("http://huha.com/minimal#birthday","dateValue"),
    STRING("String","stringValue");

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
}
