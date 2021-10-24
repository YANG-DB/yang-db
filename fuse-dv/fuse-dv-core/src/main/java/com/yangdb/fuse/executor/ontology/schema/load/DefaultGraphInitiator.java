package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.opensearch.ElasticIndexProviderMappingFactory;
import com.yangdb.fuse.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.client.Client;

import java.util.List;

import static com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema.indices;

/**
 * Init / Drop Graph Indices over E/S
 */
public class DefaultGraphInitiator implements GraphInitiator {
    private final Client client;
    private final RawSchema schema;

    private final String assembly;
    private IndexProviderFactory indexProviderFactory;
    private final OntologyProvider ontologyProvider;
    private IndexProvider indexProvider;
    private ObjectMapper objectMapper;
    private ElasticIndexProviderMappingFactory mappingFactory;

    @Inject
    public DefaultGraphInitiator(Config config, Client client, IndexProviderFactory indexProviderFactory, OntologyProvider ontologyProvider, RawSchema schema) {
        this.assembly = config.getString("assembly");
        this.indexProviderFactory = indexProviderFactory;
        this.ontologyProvider = ontologyProvider;
        this.objectMapper = new ObjectMapper();
        this.client = client;
        this.schema = schema;
        Ontology ont = ontologyProvider.get(assembly)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly)));

        //if no index provider found with assembly name - generate default one accoring to ontology and simple Static Index Partitioning strategy
        this.indexProvider = indexProviderFactory.get(assembly).orElseGet(() -> IndexProvider.Builder.generate(ont));
        this.mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ont, indexProvider);

    }

    @Override
    public long drop() {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        //refresh cluster
        client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    public long drop(String ontology) {
        Ontology ont = ontologyProvider.get(ontology)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for name",
                        "No Ontology present for name" + ontology)));

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProviderJsonFactory(indexProviderFactory, ont).get(ont);
        Iterable<String> indices = indices(schemaProvider);
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        //refresh cluster
        client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    @Override
    public long createTemplate(String ontology, String schemaProvider) {
        try {
            IndexProvider indexProvider = objectMapper.readValue(schemaProvider, IndexProvider.class);
            mappingFactory.indexProvider(indexProvider)
                    .ontology(ontologyProvider.get(ontology)
                            .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + ontology))));
            List<Tuple2<String, Boolean>> results = mappingFactory.generateMappings();
            return results.stream().filter(t -> t._2).count();
        } catch (Throwable t) {
            throw new FuseError.FuseErrorException("Create templates error " + ontology + " with schema " + schemaProvider, t);
        }
    }

    @Override
    public long createTemplate(String ontology) {
        Ontology ont = ontologyProvider.get(ontology)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for name",
                        "No Ontology present for name" + ontology)));

        IndexProvider provider = indexProviderFactory.get(assembly)
                .orElseGet(() -> IndexProvider.Builder.generate(ont));
        //generate index raw schema
        IndexProviderRawSchema rawSchema = new IndexProviderRawSchema(ont, new GraphElementSchemaProviderJsonFactory(provider, ont));
        //generate E/S mapping factory
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, rawSchema, ont, provider);
        //generate mappings
        return mappingFactory.generateMappings().size();
    }

    @Override
    public long createIndices(String ontology, String schemaProvider) {
        try {
            IndexProvider indexProvider = objectMapper.readValue(schemaProvider, IndexProvider.class);
            mappingFactory.indexProvider(indexProvider)
                    .ontology(ontologyProvider.get(ontology)
                            .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + ontology))));
            //first generate the index mapping
            mappingFactory.generateMappings();
            //create the indices
            List<Tuple2<Boolean, String>> indices = mappingFactory.createIndices();
            return indices.size();
        } catch (Throwable t) {
            throw new FuseError.FuseErrorException("Create Indices error " + ontology + " with schema " + schemaProvider, t);
        }
    }

    @Override
    public long createIndices(String ontology) {
        Ontology ont = ontologyProvider.get(ontology)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for name",
                        "No Ontology present for name" + ontology)));

        IndexProvider provider = indexProviderFactory.get(assembly)
                .orElseGet(() -> IndexProvider.Builder.generate(ont));
        //generate index raw schema
        IndexProviderRawSchema rawSchema = new IndexProviderRawSchema(ont, new GraphElementSchemaProviderJsonFactory(provider, ont));
        //generate E/S mapping factory
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, rawSchema, ont, provider);
        //create indices
        List<Tuple2<Boolean, String>> indices = mappingFactory.createIndices();
        //refresh cluster
        client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        return Stream.ofAll(indices).count(s -> !s._2.isEmpty());
    }


    @Override
    //todo check this under IT tests
    public long init() {
        //generate mappings
        List<Tuple2<String, Boolean>> mappingResults = mappingFactory.generateMappings();
        //todo log indices names
        //create indices
        List<Tuple2<Boolean, String>> indices = mappingFactory.createIndices();
        //refresh cluster
        client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        return Stream.ofAll(indices).count(s -> !s._2.isEmpty());

    }

    /**
     * @param ontology
     * @return
     */
    public long init(String ontology) {
        Ontology ont = ontologyProvider.get(ontology)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for name",
                        "No Ontology present for name" + ontology)));

        IndexProvider provider = indexProviderFactory.get(assembly)
                .orElseGet(() -> IndexProvider.Builder.generate(ont));
        //generate index raw schema
        IndexProviderRawSchema rawSchema = new IndexProviderRawSchema(ont, new GraphElementSchemaProviderJsonFactory(provider, ont));
        //generate E/S mapping factory
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, rawSchema, ont, provider);
        //generate mappings
        List<Tuple2<String, Boolean>> mappingResults = mappingFactory.generateMappings();
        //create indices
        List<Tuple2<Boolean, String>> indices = mappingFactory.createIndices();
        //refresh cluster
        client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        return Stream.ofAll(indices).count(s -> !s._2.isEmpty());
    }
}
