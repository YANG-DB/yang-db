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
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
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

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.extractFile;

/**
 * Loader for CSV Data Model to E/S
 * - load with file
 */
public class IndexProviderBasedCSVLoader implements CSVDataLoader {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private CSVTransformer transformer;
    private RawSchema schema;
    private ObjectMapper mapper;


    @Inject
    public IndexProviderBasedCSVLoader(Client client,
                                       CSVTransformer transformer,
                                       RawSchema schema) {
        this.client = client;
        this.transformer = transformer;
        this.schema = schema;
        this.mapper = new ObjectMapper();

    }

    @Override
    public LoadResponse<String, FuseError> load(String type, File data, GraphDataLoader.Directive directive) throws IOException {
        DataTransformerContext context;
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

            ByteArrayOutputStream finalStream = stream;
            context = transformer.transform(readCsv(type, new BufferedReader(new InputStreamReader(new ByteArrayInputStream(finalStream.toByteArray())))), directive);
            return load(context, directive);
        }

        context = transformer.transform(readCsv(type, new FileReader(data.getAbsoluteFile())), directive);
        return load(context, directive);
    }

    /**
     * load data into E/S
     *
     * @param context
     * @param directive
     * @return
     */
    public LoadResponse<String, FuseError> load(DataTransformerContext context, GraphDataLoader.Directive directive) {
        BulkRequestBuilder bulk = client.prepareBulk();
        Response upload = new Response("Upload");
        //load bulk requests
        load(bulk, upload, context);
        //submit bulk request
        submit(bulk, upload);

        return new LoadResponseImpl().response(context.getTransformationResponse()).response(upload);

    }

    private void submit(BulkRequestBuilder bulk, Response upload) {
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
    }

    private void load(BulkRequestBuilder bulk, Response upload, DataTransformerContext<Object> context) {
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


    private CSVTransformer.CsvElement readCsv(String type, Reader reader) {
        return new CSVTransformer.CsvElement() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public Reader content() {
                return reader;
            }
        };
    }
}
