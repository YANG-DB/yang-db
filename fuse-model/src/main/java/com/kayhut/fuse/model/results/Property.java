package com.kayhut.fuse.model.results;

/*-
 * #%L
 * Property.java - fuse-model - kayhut - 2,016
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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Property {
    //region Constructors
    public Property() {

    }

    public Property(String pType, Object value) {
        this(pType, null, value);
    }

    public Property(String pType, String agg, Object value) {
        this.pType = pType;
        this.agg = agg;
        this.value = value;
    }
    //endregion

    //region Properties
    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
        this.pType = pType;
    }

    public String getAgg() {
        return agg;
    }

    public void setAgg(String agg) {
        this.agg = agg;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return String.format("Property [pType = %s, value = %s]", this.pType, this.value);
    }
    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;
        Property property = (Property) o;
        return Objects.equals(pType, property.pType) &&
                Objects.equals(agg, property.agg) &&
                Objects.equals(value, property.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pType, agg, value);
    }

    //region Fields
    private String pType;
    private String agg;
    private Object value;
    //endregion

    public static Optional<Property> findAny(Collection<Property> properties, Predicate<Property> predicate) {
        return Stream.ofAll(properties).filter(predicate).toJavaOptional();
    }
}
