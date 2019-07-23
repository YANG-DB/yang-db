package org.unipop.util;

/*-
 * #%L
 * PropertySchemaFactory.java - unipop-core - yangdb - 2,016
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

import org.unipop.schema.property.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by sbarzilay on 8/8/16.
 */
public class PropertySchemaFactory {
    public static List<PropertySchema.PropertySchemaBuilder> builders;

    private PropertySchemaFactory(List<PropertySchema.PropertySchemaBuilder> providers, List<PropertySchema.PropertySchemaBuilder> thirdParty) {
        builders = new ArrayList<>();
        builders.add(new StaticPropertySchema.Builder());
        builders.add(new FieldPropertySchema.Builder());
        builders.add(new DateFieldPropertySchema.Builder());
        builders.add(new StaticDatePropertySchema.Builder());
        builders.add(new MultiPropertySchema.Builder());
        builders.add(new ConcatenateFieldPropertySchema.Builder());
        builders.add(new CoalescePropertySchema.Builder());
        builders.addAll(providers);
        builders.addAll(thirdParty);
        Collections.reverse(builders);
    }

    public static PropertySchema createPropertySchema(String key, Object value, AbstractPropertyContainer container) {
        if (value instanceof String){
            if (value.toString().startsWith("$")) {
                Optional<PropertySchema> reference = container.getPropertySchemas().stream()
                        .filter(schema -> schema.getKey().equals(value.toString().substring(1)))
                        .findFirst();
                 if (reference.isPresent()) return reference.get();
                else throw new IllegalArgumentException("cant find reference to: " + value.toString().substring(1));
            }
        }
        Optional<PropertySchema> first = builders.stream().map(builder -> builder.build(key, value, container)).filter(schema -> schema != null).findFirst();
        if (first.isPresent()) return first.get();
        else return null;
//        throw new IllegalArgumentException("Unrecognized property: " + key + " - " + value);
    }

    public static PropertySchemaFactory build(List<PropertySchema.PropertySchemaBuilder> providers, List<PropertySchema.PropertySchemaBuilder> thirdParty){
        return new PropertySchemaFactory(providers, thirdParty);
    }
}
