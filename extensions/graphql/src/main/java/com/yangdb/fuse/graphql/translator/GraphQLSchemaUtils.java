package com.yangdb.fuse.graphql.translator;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.ontology.Ontology;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;

public interface GraphQLSchemaUtils {
    /** a schema this instance holds (e.g. the last one it translated), if any */
    GraphQLSchema getGraphQLSchema();

    /** the type definitions of {@link #getGraphQLSchema()}, if any */
    TypeDefinitionRegistry getTypeRegistry();

    /**
     * The type definitions GraphQL queries on this ontology run against. By default, this instance's registry when it
     * has a {@code Query} type, else one generated from the ontology ({@link OntologyToGraphQLSdl}).
     */
    default TypeDefinitionRegistry getTypeRegistry(Ontology ontology) {
        TypeDefinitionRegistry registry = getTypeRegistry();
        if (registry != null && registry.getType(OntologyToGraphQLSdl.QUERY).isPresent()) {
            return registry;
        }
        return OntologyToGraphQLSdl.registry(ontology);
    }
}
