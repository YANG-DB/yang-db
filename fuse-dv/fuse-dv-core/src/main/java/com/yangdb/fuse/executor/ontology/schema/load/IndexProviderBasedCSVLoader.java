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
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderIfc;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.extractFile;

public class IndexProviderBasedCSVLoader implements CSVDataLoader {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final GraphDataLoader<String,FuseError> loader;

    private Client client;
    private EntityTransformer transformer;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private ObjectMapper mapper;
    private IdGeneratorDriver<Range> idGenerator;
    private ElasticIndexProviderMappingFactory mappingFactory;


    @Inject
    public IndexProviderBasedCSVLoader(Config config, Client client, OntologyProvider ontology,
                                       GraphDataLoader<String,FuseError> loader, EntityTransformer transformer,
                                       RawSchema schema, IndexProviderIfc indexProviderFactory,
                                       IdGeneratorDriver<Range> idGenerator) {
        this.loader = loader;
        String assembly = config.getString("assembly");
            this.client = client;
        this.transformer = transformer;
        this.schema = schema;
        this.indexProvider = indexProviderFactory.get(assembly)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Index Provider present for assembly", "No Index Provider present for assembly" + assembly)));
        Ontology ont = ontology.get(assembly)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly)));
        this.idGenerator = idGenerator;
        this.mapper = new ObjectMapper();
        this.mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ont, indexProvider);

    }

    @Override
    public LoadResponse<String, FuseError> load(File data, GraphDataLoader.Directive directive) throws IOException {
        String contentType = Files.probeContentType(data.toPath());
        if (Arrays.asList("application/gzip", "application/zip").contains(contentType)) {
            ByteArrayOutputStream stream = null; //unzip
            switch (contentType) {
                case "application/gzip":
                    stream = extractFile(new GZIPInputStream(Files.newInputStream(data.toPath())));
                    break;
                case "application/zip":
                    stream = extractFile(new ZipInputStream(Files.newInputStream(data.toPath())));
                    break;
            }

            String graph = new String(stream.toByteArray());
            return loader.load(mapper.readValue(graph, LogicalGraphModel.class), directive);
        }
        String graph = new String(Files.readAllBytes(data.toPath()));
        //read
        LogicalGraphModel root = mapper.readValue(graph, LogicalGraphModel.class);
        return loader.load(root, directive);
    }
}
