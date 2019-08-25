package com.yangdb.dragons.schema;

/*-
 * #%L
 * fuse-domain-dragons-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.GraphLayoutProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphLayoutProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DragonsRawSchema implements RawSchema {

    private Ontology ontology;
    private GraphLayoutProvider layoutProvider;
    private GraphElementSchemaProvider schemaProvider;
//    private GraphElementSchemaProvider schemaProvider;

    @Inject
    public DragonsRawSchema(Config config, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProvider, GraphLayoutProviderFactory layoutProviderFactory) {
        this.ontology = ontologyProvider.get(config.getString("assembly")).get();
        this.schemaProvider = schemaProvider.get(ontology);
        this.layoutProvider = layoutProviderFactory.get(ontology);
    }

    @Override
    public IndexPartitions getPartition(String type) {
        return schemaProvider.getVertexSchemas(type).iterator().next().getIndexPartitions().get();
    }

    @Override
    public String getIdFormat(String type) {
        return "";
    }

    @Override
    public String getPrefix(String type) {
        return "";
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                .collect(Collectors.toList());

    }

    @Override
    public Iterable<String> indices() {
        Stream<String> edges = StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false)
                .flatMap(p -> StreamSupport.stream(p.getIndexPartitions().get().getPartitions().spliterator(), false))
                .flatMap(v -> StreamSupport.stream(v.getIndices().spliterator(), false));
        Stream<String> vertices = StreamSupport.stream(schemaProvider.getVertexSchemas().spliterator(), false)
                .flatMap(p -> StreamSupport.stream(p.getIndexPartitions().get().getPartitions().spliterator(), false))
                .flatMap(v -> StreamSupport.stream(v.getIndices().spliterator(), false));

        return Stream.concat(edges,vertices)
                .collect(Collectors.toSet());
    }
}
