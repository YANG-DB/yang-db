package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.concurrent.atomic.AtomicInteger;

import static com.kayhut.fuse.assembly.knowlegde.KnowledgeDataInfraManager.PGE;

public class KnowledgeWriterContext {
    private AtomicInteger counter = new AtomicInteger(0);
    public TransportClient client;
    public RawSchema schema;
    public ObjectMapper mapper;

    public String nextLogicalId(){
        return  "e" + String.format(this.schema.getIdFormat("entity"), counter.incrementAndGet());
    }

    public static KnowledgeWriterContext init(TransportClient client, RawSchema schema) {
        final KnowledgeWriterContext context = new KnowledgeWriterContext();
        context.client = client;
        context.schema = schema;
        context.mapper = new ObjectMapper();
        return context;
    }


    public static <T extends EntityId> int commit(KnowledgeWriterContext ctx,String index, T... builders) throws JsonProcessingException {
        int count = 0;
        final BulkRequestBuilder bulk = ctx.client.prepareBulk();
        for (EntityId builder : builders) {
            IndexRequestBuilder request = ctx.client.prepareIndex()
                    .setIndex(index)
                    .setType(PGE)
                    .setId(builder.id())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setRouting(builder.logicalId)
                    .setSource(builder.toString(ctx.mapper), XContentType.JSON);
            count += bulk.add(request).get().getItems().length;
        }
        return count;


    }

}
