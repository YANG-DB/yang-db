package com.yangdb.fuse.executor.ontology.schema.load;

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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IndexProviderBasedGraphLoader implements GraphDataLoader<String, FuseError> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private EntityTransformer transformer;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private ObjectMapper mapper;
    private IdGeneratorDriver<Range> idGenerator;
    private ElasticIndexProviderMappingFactory mappingFactory;


    @Inject
    public IndexProviderBasedGraphLoader(Config config, Client client, OntologyProvider ontology, EntityTransformer transformer, RawSchema schema, IndexProviderIfc indexProviderFactory, IdGeneratorDriver<Range> idGenerator) {
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
    public long init() {
        //generate mappings
        List<Tuple2<String, Boolean>> mappingResults = mappingFactory.generateMappings();
        //todo log indices names
        //create indices
        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices()
                .create(new CreateIndexRequest(index.toLowerCase())).actionGet());

        //refresh cluster
        client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());

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

    @Override
    public LoadResponse<String, FuseError> load(LogicalGraphModel root, Directive directive) {
        BulkRequestBuilder bulk = client.prepareBulk();
        Response upload = new Response("Upload");
        DataTransformerContext context = transformer.transform(root, directive);
        //populate bulk entities documents index requests
        for (DocumentBuilder documentBuilder : context.getEntities()) {
            try {
                buildIndexRequest(bulk, documentBuilder);
            } catch (FuseError.FuseErrorException e) {
                upload.failure(e.getError());
            }
        }
        //populate bulk relations document index requests
        for (DocumentBuilder e : context.getRelations()) {
            try {
                buildIndexRequest(bulk, e);
            } catch (FuseError.FuseErrorException err) {
                upload.failure(err.getError());
            }
        }

        //bulk index data
        BulkResponse responses = bulk.get();
        final BulkItemResponse[] items = responses.getItems();
        for (BulkItemResponse item : items) {
            if (!item.isFailed()) {
                upload.success(item.getId());
            } else {
                //log error
                BulkItemResponse.Failure failure = item.getFailure();
                DocWriteRequest<?> request = bulk.request().requests().get(item.getItemId());
                //todo - get TechId from request
                upload.failure(new FuseError("commit failed", failure.toString()));
            }

        }
        return new LoadResponseImpl().response(context.getTransformationResponse()).response(upload);
    }

    private IndexRequestBuilder buildIndexRequest(BulkRequestBuilder bulk, DocumentBuilder node) {
        try {
            String index = resolveIndex(node);
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(index.toLowerCase())
                    .setType(node.getType())
                    .setId(node.getId())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(mapper.writeValueAsString(node.getNode()), XContentType.JSON);
            node.getRouting().ifPresent(request::setRouting);
            bulk.add(request);
            return request;
        } catch (Throwable err) {
            throw new FuseError.FuseErrorException("Error while building Index request", err,
                    new FuseError("Error while building Index request", err.getMessage()));
        }
    }

    /**
     * resolve index name according to schema and in case of range partitioned index - according to the partitioning field value
     *
     * @param node
     * @return
     */
    private String resolveIndex(DocumentBuilder node) throws ParseException {
        String nodeType = node.getType();
        Optional<Tuple2<String, String>> field = node.getPartitionField();
        IndexPartitions partitions = schema.getPartition(nodeType);
        //todo validate the partitioned field is indeed the correct time field
        if ((partitions instanceof TimeSeriesIndexPartitions) && field.isPresent()) {
            String indexName = ((TimeSeriesIndexPartitions) partitions).getIndexName(sdf.parse(field.get()._2));
            if (indexName != null) return indexName;
        }
        return partitions.getPartitions().iterator().next().getIndices().iterator().next();
    }


    @Override
    public LoadResponse<String, FuseError> load(File data, Directive directive) {
        return LoadResponse.EMPTY;
    }

    public Range.StatefulRange getRange(String type) {
        //init ranges
        Range.StatefulRange statefulRange = ranges.computeIfAbsent(type,
                s -> new Range.StatefulRange(idGenerator.getNext(type, 1000)));

        if (statefulRange.hasNext())
            return statefulRange;
        //update ranges
        ranges.put(type, new Range.StatefulRange(idGenerator.getNext(type, 1000)));
        //return next range
        return ranges.get(type);
    }

}
