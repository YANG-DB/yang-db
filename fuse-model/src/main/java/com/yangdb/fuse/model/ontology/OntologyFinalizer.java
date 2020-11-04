package com.yangdb.fuse.model.ontology;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * OntologyFinalizer.java - fuse-model - yangdb - 2,016
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
        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(ID_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(ID_FIELD_NAME).withPType(ID_FIELD_PTYPE).withType("string").build());

        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(TYPE_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(TYPE_FIELD_PTYPE).withPType(TYPE_FIELD_PTYPE).withType("string").build());

        Stream.ofAll(ontology.getEntityTypes())
                .forEach(entityType -> {
                    if (entityType.fields().stream().noneMatch(p -> p.equals(ID_FIELD_PTYPE))) {
                        entityType.getProperties().add(ID_FIELD_PTYPE);
                    }
                    if (entityType.fields().stream().noneMatch(p -> p.equals(TYPE_FIELD_PTYPE))) {
                        entityType.getProperties().add(TYPE_FIELD_PTYPE);
                    }
                });

        return ontology;
    }
}
