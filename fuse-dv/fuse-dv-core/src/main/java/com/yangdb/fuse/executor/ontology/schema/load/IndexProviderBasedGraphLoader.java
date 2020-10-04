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
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.Tuple2;
import org.elasticsearch.action.DocWriteRequest;
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

/**
 * Loader for Graph Data Model to E/S
 *  - load directly with Json structure
 *  - load with file
 */
public class IndexProviderBasedGraphLoader implements GraphDataLoader<String, FuseError> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final int NUM_IDS = 1000;
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private EntityTransformer transformer;
    private RawSchema schema;
    private ObjectMapper mapper;
    private IdGeneratorDriver<Range> idGenerator;


    @Inject
    public IndexProviderBasedGraphLoader(Client client, EntityTransformer transformer, RawSchema schema, IdGeneratorDriver<Range> idGenerator) {
        this.client = client;
        this.schema = schema;
        this.transformer = transformer;
        this.idGenerator = idGenerator;
        this.mapper = new ObjectMapper();

    }

    @Override
    public LoadResponse<String, FuseError> load(String ontology, LogicalGraphModel root, Directive directive) {
        //todo load correct ontology graph transformer and use it to transform data to the actual schema structure
        BulkRequestBuilder bulk = client.prepareBulk();
        Response upload = new Response("Upload");
        DataTransformerContext<LogicalGraphModel> context = transformer.transform(root, directive);
        //load bulk requests
        load(bulk, upload, context);
        //submit bulk request
        submit(bulk, upload);

        return new LoadResponseImpl().response(context.getTransformationResponse()).response(upload);
    }

    private void submit(BulkRequestBuilder bulk, Response upload) {
        //bulk index data
        try {
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
        }catch (Exception err) {
            upload.failure(new FuseError("commit failed", err.toString()));
        }
    }

    private void load(BulkRequestBuilder bulk, Response upload, DataTransformerContext<LogicalGraphModel> context) {
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
    }

    public IndexRequestBuilder buildIndexRequest(BulkRequestBuilder bulk, DocumentBuilder node) {
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
        //get the first matching index to populate
        return partitions.getIndices().iterator().next();
    }


    @Override
    public LoadResponse<String, FuseError> load(String ontology, File data, Directive directive) throws IOException {
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
            return load(ontology , mapper.readValue(graph, LogicalGraphModel.class), directive);
        }
        String graph = new String(Files.readAllBytes(data.toPath()));
        //read
        LogicalGraphModel root = mapper.readValue(graph, LogicalGraphModel.class);
        return load(ontology, root, directive);
    }


    public Range.StatefulRange getRange(String type) {
        //init ranges
        Range.StatefulRange statefulRange = ranges.computeIfAbsent(type,
                s -> new Range.StatefulRange(idGenerator.getNext(type, NUM_IDS)));

        if (statefulRange.hasNext())
            return statefulRange;
        //update ranges
        ranges.put(type, new Range.StatefulRange(idGenerator.getNext(type, NUM_IDS)));
        //return next range
        return ranges.get(type);
    }

}
