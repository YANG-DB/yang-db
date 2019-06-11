package org.unipop.schema.property;

/*-
 * #%L
 * ParentSchemaProperty.java - unipop-core - kayhut - 2,016
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sbarzilay on 8/2/16.
 */
public interface ParentSchemaProperty extends PropertySchema{
    Collection<PropertySchema> getChildren();

    default Set<String> excludeDynamicFields() {
        return getChildren().stream()
                .map(PropertySchema::excludeDynamicFields)
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    default Set<String> toFields(Set<String> propertyKeys) {
        return getChildren().stream().flatMap(s -> s.toFields(propertyKeys).stream()).collect(Collectors.toSet());
    }

    default Map<String, Object> toFields(Map<String, Object> properties){
        return Collections.emptyMap();
    }
}
