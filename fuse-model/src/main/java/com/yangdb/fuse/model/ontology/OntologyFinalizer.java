package com.yangdb.fuse.model.ontology;

/*-
 * #%L
 * OntologyFinalizer.java - fuse-model - yangdb - 2,016
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

import javaslang.collection.Stream;

import java.util.Arrays;

/**
 * Created by moti on 5/14/2017.
 */
public class OntologyFinalizer {

    public static final String ID_FIELD_PTYPE = "id";
    public static final String TYPE_FIELD_PTYPE = "type";

    public static final String ID_FIELD_NAME = "id";
    public static final String TYPE_FIELD_NAME = "type";

    public static Ontology finalize(Ontology ontology) {
        ontology.setProperties(Stream.ofAll(ontology.getProperties())
                .append(Property.Builder.get().withName(ID_FIELD_NAME).withPType(ID_FIELD_PTYPE).withType("string").build())
                .append(Property.Builder.get().withName(TYPE_FIELD_NAME).withPType(TYPE_FIELD_PTYPE).withType("string").build())
                .distinct()
                .toJavaList());

        Stream.ofAll(ontology.getEntityTypes())
                .forEach(entityType -> entityType.setProperties(
                        Stream.ofAll(entityType.getProperties())
                                .appendAll(Arrays.asList(ID_FIELD_PTYPE, TYPE_FIELD_PTYPE))
                                .distinct()
                                .toJavaList()
                ));

        return ontology;
    }
}
