package com.kayhut.fuse.assembly.knowledge.load;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.assembly.knowledge.load.builder.*;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



public class KnowledgeWriterContext {
    public static final String PGE = "pge";

    private AtomicInteger eCounter = new AtomicInteger(0);
    private AtomicInteger evCounter = new AtomicInteger(0);
    private AtomicInteger fCounter = new AtomicInteger(0);
    private AtomicInteger refCounter = new AtomicInteger(0);
    private AtomicInteger relCounter = new AtomicInteger(0);
    private AtomicInteger iCounter = new AtomicInteger(0);

    public Client client;
    public RawSchema schema;
    public List<Items> created;
    private KnowledgeContext context;


    public KnowledgeWriterContext() {
        context = new KnowledgeContext();
    }

    public KnowledgeWriterContext(KnowledgeContext context) {
        this.context = context;
    }

    public ValueBuilder v(){
        final ValueBuilder builder = ValueBuilder._v(nextValueId());
        context.add(builder);
        return builder;
    }

    public RvalueBuilder r(){
        final RvalueBuilder builder = RvalueBuilder._r(nextRvalueId());
        context.add(builder);
        return builder;
    }

    public RelationBuilder rel(){
        final RelationBuilder builder = RelationBuilder._rel(nextRelId());
        context.add(builder);
        return builder;
    }

    public EntityBuilder e() {
        EntityBuilder e = EntityBuilder._e(nextLogicalId());
        context.add(e);
        return e;
    }

    public KnowledgeWriterContext rel(RelationBuilder... builders) {
        context.addAll(Arrays.asList(builders));
        return this;
    }

    public String nextLogicalId(RawSchema schema,long index) {
        return "e" + String.format(schema.getIdFormat("entity"), index);
    }

    public String nextLogicalId() {
        return nextLogicalId(schema,eCounter.incrementAndGet());
    }

    public String nextValueId(RawSchema schema,long index) {
        return "ev" + String.format(schema.getIdFormat("entity"), index);
    }

    public String nextValueId() {
        return nextValueId(schema, evCounter.incrementAndGet());
    }

    public String nextRvalueId(RawSchema schema,long index) {
        return "rv" + String.format(schema.getIdFormat("relation"), index);
    }

    public String nextRvalueId() {
        return nextRvalueId( schema,evCounter.incrementAndGet());
    }

    public String nextRefId(RawSchema schema,long index) {
        return "ref" + String.format(schema.getIdFormat("reference"), index);
    }

    public String nextRefId() {
        return nextRefId(schema,refCounter.incrementAndGet());
    }

    public String nextInsightId(RawSchema schema,long index) {
        return "i" + String.format(schema.getIdFormat("insight"), index);
    }

    public String nextInsightId() {
        return  nextInsightId(schema,iCounter.incrementAndGet());
    }

    public String nextRelId(RawSchema schema,long index) {
        return "r" + String.format(schema.getIdFormat("relation"), index);
    }

    public String nextRelId() {
        return nextRelId(schema, relCounter.incrementAndGet());
    }

    public String nextFileId(RawSchema schema,long index) {
        return "f" + String.format(schema.getIdFormat("e.file"), index);
    }

    public String nextFileId() {
        return nextFileId(schema,fCounter.incrementAndGet());
    }

    public static KnowledgeWriterContext init(Client client, RawSchema schema) {
        final KnowledgeWriterContext context = new KnowledgeWriterContext();
        context.client = client;
        context.schema = schema;
        context.created = new ArrayList<>();
        return context;
    }

    private static void populateBulk(BulkRequestBuilder bulk,String index,Client client,List<KnowledgeDomainBuilder> builders,ObjectMapper mapper) throws JsonProcessingException {
        for (KnowledgeDomainBuilder builder : builders) {
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(index)
                    .setType(PGE)
                    .setId(builder.id())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(builder.toString(mapper), XContentType.JSON);
            builder.routing().ifPresent(request::setRouting);
            bulk.add(request);
        }
    }

    public static <T extends KnowledgeDomainBuilder> int commit(Client client, String index, List<T> builders, ObjectMapper mapper) throws JsonProcessingException {
        int count = 0;
        final BulkRequestBuilder bulk = client.prepareBulk();
        count = process(client, index, count, bulk, builders,mapper);
        return count;
    }

    public static <T extends KnowledgeDomainBuilder> int commit(Client client, String index, ObjectMapper mapper, T... builders) throws JsonProcessingException {
        int count = 0;
        final BulkRequestBuilder bulk = client.prepareBulk();
        count = process(client, index, count, bulk, Arrays.asList(builders),mapper);
        return count;
    }

    private static <T extends KnowledgeDomainBuilder> int process(Client client, String index, int count, BulkRequestBuilder bulk, List<T> builders, ObjectMapper mapper) throws JsonProcessingException {
        populateBulk(bulk,index,client, (List<KnowledgeDomainBuilder>) builders,mapper);
        builders.forEach(builder -> {
            try {
                populateBulk(bulk,index,client,builder.additional(),mapper);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        final BulkItemResponse[] items = bulk.get().getItems();
        for (BulkItemResponse item : items) {
            if (!item.isFailed()) {
                count++;
            }

        }
        client.admin().indices().prepareRefresh(index).get();
        return count;
    }

    public static class Items {
        public String index;
        public String type;
        public String id;

        public Items(String index, String type, String id) {
            this.index = index;
            this.type = type;
            this.id = id;
        }
    }
}
