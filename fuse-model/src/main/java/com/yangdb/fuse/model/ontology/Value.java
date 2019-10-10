package com.yangdb.fuse.model.ontology;

/*-
 *
 * Value.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Value {

    public Value() {
    }

    public Value(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Value [val = "+val+", name = "+name+"]";
    }

    //region Fields
    private int val;
    private String name;
    //endregion

    public static final class ValueBuilder {
        private int val;
        private String name;

        private ValueBuilder() {
        }

        public static ValueBuilder aValue() {
            return new ValueBuilder();
        }

        public ValueBuilder withVal(int val) {
            this.val = val;
            return this;
        }

        public ValueBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public Value build() {
            return new Value(val,name);
        }
    }
}
