package com.kayhut.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NamedParameter {
    private String name;
    private Object value;

    public NamedParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    public NamedParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedParameter)) return false;
        NamedParameter that = (NamedParameter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
