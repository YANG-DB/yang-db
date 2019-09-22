package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
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
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IndexProviderRawSchema implements RawSchema {

    private GraphElementSchemaProvider schemaProvider;

    @Inject
    public IndexProviderRawSchema(Config config, OntologyProvider provider, GraphElementSchemaProviderFactory schemaProviderFactory ) {
        String assembly = config.getString("assembly");
        Optional<Ontology> ontology = provider.get(assembly);
        Ontology ont = ontology.orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly)));
        this.schemaProvider = schemaProviderFactory.get(ont);
    }

    @Override
    public IndexPartitions getPartition(String type) {
        if(schemaProvider.getVertexSchemas(type).iterator().hasNext())
            return schemaProvider.getVertexSchemas(type).iterator().next().getIndexPartitions().get();
        if(schemaProvider.getEdgeSchemas(type).iterator().hasNext())
            return schemaProvider.getEdgeSchemas(type).iterator().next().getIndexPartitions().get();

        throw new FuseError.FuseErrorException("No valid partition found for " + type,new FuseError("IndexProvider Schema Error","No valid partition found for " + type));

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
