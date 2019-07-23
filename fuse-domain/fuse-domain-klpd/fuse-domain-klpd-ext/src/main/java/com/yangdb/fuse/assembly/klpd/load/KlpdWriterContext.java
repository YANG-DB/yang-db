package com.yangdb.fuse.assembly.klpd.load;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.klpd.KnowledgeRawSchema;
import com.yangdb.fuse.assembly.klpd.load.builder.*;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
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


public class KlpdWriterContext {
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
    private KlpdContext context;


    public KlpdWriterContext() {
        context = new KlpdContext();
    }

    public KlpdWriterContext(KlpdContext context) {
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

    public KlpdWriterContext rel(RelationBuilder... builders) {
        context.addAll(Arrays.asList(builders));
        return this;
    }

    public KlpdContext getContext() {
        return context;
    }

    public String nextLogicalId(RawSchema schema, long index) {
        return format(schema, index, "e", "entity");
    }

    public String nextLogicalId() {
        return nextLogicalId(schema,eCounter.incrementAndGet());
    }

    public String nextValueId(RawSchema schema,long index) {
        return format(schema, index, "ev", "entity");
    }

    public String nextValueId() {
        return nextValueId(schema, evCounter.incrementAndGet());
    }

    public String nextRvalueId(RawSchema schema,long index) {
        return format(schema, index, "rv", "relation");
    }

    public String nextRvalueId() {
        return nextRvalueId( schema,evCounter.incrementAndGet());
    }

    public String nextRefId(RawSchema schema,long index) {
        return format(schema, index, "ref", "reference");
    }

    public String nextRefId() {
        return nextRefId(schema,refCounter.incrementAndGet());
    }

    public String nextInsightId(RawSchema schema,long index) {
        return format(schema, index, "i", "insight");
    }

    public String nextInsightId() {
        return  nextInsightId(schema,iCounter.incrementAndGet());
    }

    public String nextRelId(RawSchema schema,long index) {
        return format(schema, index, "r", "relation");
    }

    public static String format(RawSchema schema, long index, String prefix, String type) {
        return prefix + String.format(schema.getIdFormat(type), index);
    }

    public static String format(RawSchema schema, String index, String prefix, String type) {
        return prefix + String.format(schema.getIdFormat(type), index);
    }

    public String nextRelId() {
        return nextRelId(schema, relCounter.incrementAndGet());
    }

    public String nextFileId(RawSchema schema,long index) {
        return format(schema, index, "f", "e.file");
    }

    public String nextFileId() {
        return nextFileId(schema,fCounter.incrementAndGet());
    }

    public static KlpdWriterContext init(Client client, RawSchema schema) {
        final KlpdWriterContext context = new KlpdWriterContext();
        context.client = client;
        context.schema = schema;
        context.created = new ArrayList<>();
        return context;
    }

    private static void populateBulk(BulkRequestBuilder bulk, String index, Client client, List<KnowledgeDomainBuilder> builders, ObjectMapper mapper) throws JsonProcessingException {
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

    public static <T extends KnowledgeDomainBuilder> int commit(Client client, String index, ObjectMapper mapper, List<T> builders) throws JsonProcessingException {
        int count = 0;
        if(builders.isEmpty())
            return count;

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

    /**
     * commit all entities and relations with their properties to the DB according to schema partition
     * - currently only first partition is uses
     * @param client
     * @param schema
     * @param mapper
     * @param context
     * @return
     * @throws JsonProcessingException
     */
    public static int commit(Client client, RawSchema schema, ObjectMapper mapper, KlpdContext context) throws JsonProcessingException {
        int count = 0;
        count += commit(client,schema.getPartition(KnowledgeRawSchema.ENTITY).getPartitions().iterator().next().getIndices().iterator().next(),mapper,context.getEntities());
        count += commit(client,schema.getPartition(KnowledgeRawSchema.EVALUE).getPartitions().iterator().next().getIndices().iterator().next(),mapper,context.geteValues());
        count += commit(client,schema.getPartition(KnowledgeRawSchema.RELATION).getPartitions().iterator().next().getIndices().iterator().next(),mapper,context.getRelations());
        count += commit(client,schema.getPartition(KnowledgeRawSchema.RVALUE).getPartitions().iterator().next().getIndices().iterator().next(),mapper,context.getrValues());
        //todo populate insight and references
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
