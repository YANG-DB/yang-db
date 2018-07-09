package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    public List<Items> created;
    private List<EntityBuilder> entities;
    private List<RelationBuilder> relations;

    public KnowledgeWriterContext() {
        entities = new ArrayList<>();
        relations = new ArrayList<>();
    }

    public EntityBuilder e() {
        EntityBuilder e = EntityBuilder._e(nextLogicalId());
        entities.add(e);
        return e;
    }

    public KnowledgeWriterContext rel(RelationBuilder... builders) {
        relations.addAll(Arrays.asList(builders));
        return this;
    }

    public int removeCreated() {
        int[] count = new int[]{0};
        List<String> indices = created.stream().map(item -> item.index).collect(Collectors.toList());
        created.forEach(entity -> {
            try {
                final DeleteResponse deleteResponse = client.delete(client.prepareDelete().setIndex(entity.index).setType(entity.type).setId(entity.id).request()).get();
                count[0] += deleteResponse.status() == RestStatus.OK ? 1 : 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        client.admin().indices().prepareRefresh(indices.toArray(new String[indices.size()])).get();
        return count[0];
    }

    public String nextLogicalId() {
        return "e" + String.format(this.schema.getIdFormat("entity"), eCounter.incrementAndGet());
    }

    public String nextValueId() {
        return "ev" + String.format(this.schema.getIdFormat("entity"), evCounter.incrementAndGet());
    }

    public String nextRefId() {
        return "ref" + String.format(this.schema.getIdFormat("reference"), refCounter.incrementAndGet());
    }

    public String nextInsightId() {
        return "i" + String.format(this.schema.getIdFormat("insight"), iCounter.incrementAndGet());
    }

    public String nextRelId() {
        return "r" + String.format(this.schema.getIdFormat("relation"), relCounter.incrementAndGet());
    }

    public String nextFileId() {
        return "f" + String.format(this.schema.getIdFormat("e.file"), fCounter.incrementAndGet());
    }

    public static KnowledgeWriterContext init(TransportClient client, RawSchema schema) {
        final KnowledgeWriterContext context = new KnowledgeWriterContext();
        context.client = client;
        context.schema = schema;
        context.mapper = new ObjectMapper();
        context.created = new ArrayList<>();
        return context;
    }


    public static <T extends KnowledgeDomainBuilder> int commit(KnowledgeWriterContext ctx, String index, T... builders) throws JsonProcessingException {
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
            bulk.add(request);
        }

        final BulkItemResponse[] items = bulk.get().getItems();
        for (BulkItemResponse item : items) {
            if (!item.isFailed()) {
                ctx.created.add(new Items(item.getIndex(), item.getType(), item.getId()));
                count++;
            }

        }
        ctx.client.admin().indices().prepareRefresh(index).get();
        return count;


    }

    public void clearCreated() {
        created.clear();
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
