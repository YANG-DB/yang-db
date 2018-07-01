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

import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeDataInfraManager.PGE;


public class KnowledgeWriterContext {
    private AtomicInteger eCounter = new AtomicInteger(0);
    private AtomicInteger evCounter = new AtomicInteger(0);
    private AtomicInteger fCounter = new AtomicInteger(0);
    private AtomicInteger refCounter = new AtomicInteger(0);
    private AtomicInteger relCounter = new AtomicInteger(0);
    private AtomicInteger iCounter = new AtomicInteger(0);

    public TransportClient client;
    public RawSchema schema;
    public ObjectMapper mapper;

    public String nextLogicalId(){
        return  "e" + String.format(this.schema.getIdFormat("entity"), eCounter.incrementAndGet());
    }

    public String nextValueId(){
        return  "ev" + String.format(this.schema.getIdFormat("entity"), evCounter.incrementAndGet());
    }

    public String nextRefId(){
        return  "ref" + String.format(this.schema.getIdFormat("reference"), refCounter.incrementAndGet());
    }

    public String nextInsightId(){
        return  "i" + String.format(this.schema.getIdFormat("insight"), iCounter.incrementAndGet());
    }

    public String nextRelId(){
        return  "i" + String.format(this.schema.getIdFormat("relation"), relCounter.incrementAndGet());
    }

    public String nextFileId(){
        return  "f" + String.format(this.schema.getIdFormat("file"), fCounter.incrementAndGet());
    }

    public static KnowledgeWriterContext init(TransportClient client, RawSchema schema) {
        final KnowledgeWriterContext context = new KnowledgeWriterContext();
        context.client = client;
        context.schema = schema;
        context.mapper = new ObjectMapper();
        return context;
    }


    public static <T extends KnowledgeDomainBuilder> int commit(KnowledgeWriterContext ctx,String index, T... builders) throws JsonProcessingException {
        int count = 0;
        final BulkRequestBuilder bulk = ctx.client.prepareBulk();
        for (KnowledgeDomainBuilder builder : builders) {
            IndexRequestBuilder request = ctx.client.prepareIndex()
                    .setIndex(index)
                    .setType(PGE)
                    .setId(builder.id())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(builder.toString(ctx.mapper), XContentType.JSON);
            builder.routing().ifPresent(request::setRouting);
            count += bulk.add(request).get().getItems().length;
        }
        return count;


    }


}
