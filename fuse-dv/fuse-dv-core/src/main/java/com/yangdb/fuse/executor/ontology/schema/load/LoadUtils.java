package com.yangdb.fuse.executor.ontology.schema.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.executor.ontology.schema.PartitionResolver;
import com.yangdb.fuse.model.GlobalConstants;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

/**
 * documents bulk loading utilities
 */
public class LoadUtils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Tuple2<Response,BulkRequestBuilder> load(PartitionResolver schema, Client client, DataTransformerContext<Object> context) {
        Response upload = new Response("upload");
        BulkRequestBuilder bulk = client.prepareBulk();
        //populate bulk entities documents index requests
        for (DocumentBuilder documentBuilder : context.getEntities()) {
            try {
                if(documentBuilder.isSuccess())
                    buildIndexRequest(schema,client,bulk, documentBuilder);
            } catch (FuseError.FuseErrorException e) {
                upload.failure(e.getError());
            }
        }
        //populate bulk relations document index requests
        for (DocumentBuilder e : context.getRelations()) {
            try {
                if(e.isSuccess())
                    buildIndexRequest(schema,client,bulk, e);
            } catch (FuseError.FuseErrorException err) {
                upload.failure(err.getError());
            }
        }
        return new Tuple2<>(upload,bulk);
    }

    public static IndexRequestBuilder buildIndexRequest(PartitionResolver schema, Client client, BulkRequestBuilder bulk, DocumentBuilder node) {
        try {
            String index = resolveIndex(schema,node);
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
    public static String resolveIndex(PartitionResolver schema, DocumentBuilder node) throws ParseException {
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

    /**
     * submit bulk request
     * @param bulk
     * @param upload
     * @return
     */
    public static void submit(BulkRequestBuilder bulk, Response upload) {
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
                    //todo - get additional information from request
                    upload.failure(new FuseError("commit failed", failure.toString()));
                }
            }
        } catch (Exception err) {
            upload.failure(new FuseError("commit failed", err.toString()));
        }
    }

}
