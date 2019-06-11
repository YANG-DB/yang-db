package org.unipop.util;

/*-
 * #%L
 * PropertyTypeFactory.java - unipop-core - kayhut - 2,016
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

import org.unipop.schema.property.type.PropertyType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sbarzilay on 8/19/16.
 */
public class PropertyTypeFactory {
    private static Set<PropertyType> propertyTypes;

    public static void init(List<String> types){
        propertyTypes = new HashSet<>();
        for (String type : types) {
            try {
                PropertyType propertyType = Class.forName(type).asSubclass(PropertyType.class).newInstance();
                propertyTypes.add(propertyType);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new IllegalArgumentException("class: '" + type + "' not found");
            }
        }
    }

    public static PropertyType getType(String typeName) throws IllegalAccessException, InstantiationException {
        for (PropertyType propertyType : propertyTypes) {
            if (propertyType.getType().equals(typeName.toUpperCase())) return propertyType;
        }
        throw new IllegalArgumentException("Property type: '" + typeName + "' does not exists");
    }
}
