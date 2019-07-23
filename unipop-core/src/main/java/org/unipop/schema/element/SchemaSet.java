package org.unipop.schema.element;

/*-
 * #%L
 * SchemaSet.java - unipop-core - yangdb - 2,016
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

import java.util.HashSet;
import java.util.Set;

public class SchemaSet {

    Set<ElementSchema> schemas = new HashSet<>();

    public void add(ElementSchema schema){
        this.schemas.add(schema);
    }

    public Set<ElementSchema> get(Boolean recursive) {
        if(!recursive) return schemas;

        Set<ElementSchema> result = new HashSet<>();
        addRecursive(result, this.schemas);
        return result;
    }

    private void addRecursive(Set<ElementSchema> result, Set<ElementSchema> schemas) {
        schemas.forEach(schema -> {
            if(result.contains(schema)) return;
            result.add(schema);
            Set childSchemas = schema.getChildSchemas();
            addRecursive(result, childSchemas);
        });
    }

    public <T extends ElementSchema> Set<T> get(Class<? extends T> c, Boolean recursive){
        Set<T> result = new HashSet<>();
        this.get(recursive).forEach(schema -> {
            if(c.isAssignableFrom(schema.getClass())) result.add((T)schema);
        });
        return result;
    }
}
