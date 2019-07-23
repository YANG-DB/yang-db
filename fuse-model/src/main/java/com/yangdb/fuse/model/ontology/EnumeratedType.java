package com.yangdb.fuse.model.ontology;

/*-
 * #%L
 * EnumeratedType.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EnumeratedType {
    public EnumeratedType() {
    }

    public EnumeratedType(String eType, List<Value> values) {
        this.eType = eType;
        this.values = values;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    @Override
    public String toString()
    {
        return "EnumeratedType [values = "+values+", eType = "+eType+"]";
    }

    //region Fields
    private String eType;
    private List<Value> values;
    //endregion

    public static EnumeratedType from(String name,Enum[] enums) {
        return new EnumeratedType(name, Arrays.stream(enums).map(v-> new Value(v.ordinal(), v.name())).collect(Collectors.toList()));
    }

    public static final class EnumeratedTypeBuilder {
        private String eType;
        private List<Value> values;

        private EnumeratedTypeBuilder() {
        }

        public static EnumeratedTypeBuilder anEnumeratedType() {
            return new EnumeratedTypeBuilder();
        }

        public EnumeratedTypeBuilder withEType(String eType) {
            this.eType = eType;
            return this;
        }

        public EnumeratedTypeBuilder withValues(List<Value> values) {
            this.values = values;
            return this;
        }

        public EnumeratedType build() {
            return new EnumeratedType(this.eType,values);
        }
    }


}
