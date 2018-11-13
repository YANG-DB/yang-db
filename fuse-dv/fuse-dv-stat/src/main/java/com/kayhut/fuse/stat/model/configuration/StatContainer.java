package com.kayhut.fuse.stat.model.configuration;

/*-
 * #%L
 * fuse-dv-stat
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatContainer {

    //region Getters & Setters
    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }
    //endregion

    //region Fields
    private List<Mapping> mappings;
    private List<Type> types;
    //endregion

    //region Builder
    public static final class Builder {
        private List<Mapping> mappings;
        private List<Type> types;

        private Builder() {
            this.mappings = new ArrayList<>();
            this.types = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withMappings(List<Mapping> mappings) {
            this.mappings = mappings;
            return this;
        }

        public Builder withMapping(Mapping mapping) {
            this.mappings.add(mapping);
            return this;
        }

        public Builder withTypes(List<Type> types) {
            this.types = types;
            return this;
        }

        public Builder withType(Type type) {
            this.types.add(type);
            return this;
        }

        public StatContainer build() {
            StatContainer statContainer = new StatContainer();
            statContainer.setMappings(mappings);
            statContainer.setTypes(types);
            return statContainer;
        }
    }
    //endregion

    
}
