package com.yangdb.fuse.assembly.knowledge.load;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.executor.ontology.schema.*;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.results.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.load.Response;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.results.LoadResponse.LoadResponseImpl;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort.*;


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

    public ValueBuilder v() {
        final ValueBuilder builder = ValueBuilder._v(nextValueId());
        context.add(builder);
        return builder;
    }

    public RvalueBuilder r() {
        final RvalueBuilder builder = RvalueBuilder._r(nextRvalueId());
        context.add(builder);
        return builder;
    }

    public RelationBuilder rel() {
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

    public KnowledgeContext getContext() {
        return context;
    }

    public String nextLogicalId(RawSchema schema, long index) {
        return schema.getIdPrefix(ENTITY) + String.format(schema.getIdFormat(ENTITY), index);
    }

    public String nextLogicalId() {
        return nextLogicalId(schema, eCounter.incrementAndGet());
    }

    public String nextValueId(RawSchema schema, long index) {
        return schema.getIdPrefix(EVALUE) + String.format(schema.getIdFormat(EVALUE), index);
    }

    public String nextValueId() {
        return nextValueId(schema, evCounter.incrementAndGet());
    }

    public String nextRvalueId(RawSchema schema, long index) {
        return schema.getIdPrefix(RVALUE) + String.format(schema.getIdFormat(RVALUE), index);
    }

    public String nextRvalueId() {
        return nextRvalueId(schema, evCounter.incrementAndGet());
    }

    public String nextRefId(RawSchema schema, long index) {
        return schema.getIdPrefix(REFERENCE) + String.format(schema.getIdFormat(REFERENCE), index);
    }

    public String nextRefId() {
        return nextRefId(schema, refCounter.incrementAndGet());
    }

    public String nextInsightId(RawSchema schema, long index) {
        return schema.getIdPrefix(INSIGHT) + String.format(schema.getIdFormat(INSIGHT), index);
    }

    public String nextInsightId() {
        return nextInsightId(schema, iCounter.incrementAndGet());
    }

    public String nextRelId(RawSchema schema, long index) {
        return schema.getIdPrefix(RELATION) + String.format(schema.getIdFormat(RELATION), index);
    }

    public String nextRelId() {
        return nextRelId(schema, relCounter.incrementAndGet());
    }

    public String nextFileId(RawSchema schema, long index) {
        return schema.getIdPrefix(EFILE) + String.format(schema.getIdFormat(EFILE), index);
    }

    public String nextFileId() {
        return nextFileId(schema, fCounter.incrementAndGet());
    }

    public static KnowledgeWriterContext init(Client client, RawSchema schema) {
        final KnowledgeWriterContext context = new KnowledgeWriterContext();
        context.client = client;
        context.schema = schema;
        context.created = new ArrayList<>();
        return context;
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
        created.clear();
        return count[0];
    }


    private static void populateBulk(BulkRequestBuilder bulk, RawSchema schema, String indexCategory, Client client, List<KnowledgeDomainBuilder> builders, ObjectMapper mapper, GraphDataLoader.Directive directive) throws JsonProcessingException {
        for (KnowledgeDomainBuilder builder : builders) {
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(resolveIndexByLabelAndId(indexCategory,
                            builder.routing().orElseGet(builder::id), schema))
                    .setType(PGE)
                    .setId(builder.id())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(builder.toString(mapper), XContentType.JSON);
            builder.routing().ifPresent(request::setRouting);
            bulk.add(request);
        }
    }

    public static <T extends KnowledgeDomainBuilder> LoadResponse.CommitResponse commit(Client client, RawSchema schema, String indexCategory, ObjectMapper mapper, List<T> builders, GraphDataLoader.Directive directive) throws JsonProcessingException {
        if (builders.isEmpty())
            return LoadResponse.CommitResponse.EMPTY;

        final BulkRequestBuilder bulk = client.prepareBulk();
        return process(client, schema, indexCategory, bulk, builders, mapper, directive);
    }


    /**
     * commit all entities and relations with their properties to the DB according to schema partition
     * - currently only first partition is uses
     *
     * @param client
     * @param schema
     * @param mapper
     * @param context
     * @param directive
     * @return
     * @throws JsonProcessingException
     */
    public static LoadResponse<String, FuseError> commit(Client client, RawSchema schema, ObjectMapper mapper, KnowledgeContext context, GraphDataLoader.Directive directive) throws JsonProcessingException {
        LoadResponse<String, FuseError> responses = new LoadResponseImpl();
        responses.response(commit(client, schema, ENTITY, mapper, context.getEntities(), directive));
        responses.response(commit(client, schema, ENTITY, mapper, context.getRelationBuilders(), directive));

        responses.response(commit(client, schema, ENTITY, mapper, context.geteValues(), directive));
        responses.response(commit(client, schema, RELATION, mapper, context.getRelations(), directive));
        responses.response(commit(client, schema, RELATION, mapper, context.getrValues(), directive));
        //todo populate insight and references
        return responses;
    }

    private static <T extends KnowledgeDomainBuilder> Response process(Client client, RawSchema schema, String indexCategory, BulkRequestBuilder bulk, List<T> builders, ObjectMapper mapper, GraphDataLoader.Directive directive) throws JsonProcessingException {
        Response response = new Response(indexCategory);
        populateBulk(bulk, schema, indexCategory, client, (List<KnowledgeDomainBuilder>) builders, mapper, directive);
        builders.forEach(builder -> {
            try {
                populateBulk(bulk, schema, indexCategory, client, builder.additional(), mapper, directive);
            } catch (JsonProcessingException e) {
                response.failure(new FuseError("commit build proccess failed", e));
            }
        });

        final BulkItemResponse[] items = bulk.get().getItems();
        for (BulkItemResponse item : items) {
            if (!item.isFailed()) {
                response.success(item.getId());
            } else {
                //log error
                BulkItemResponse.Failure failure = item.getFailure();
                DocWriteRequest<?> request = bulk.request().requests().get(item.getItemId());
                //todo - get TechId from request
                response.failure(new FuseError("commit failed", failure.toString()));
            }

        }
        Set<String> indices = Arrays.stream(bulk.get().getItems()).map(BulkItemResponse::getIndex).collect(Collectors.toSet());
        client.admin().indices().prepareRefresh(indices.toArray(new String[0])).get();
        return response;

    }

    private static String resolveIndexByLabelAndId(String indexCategory, String id, RawSchema schema) {
        List<IndexPartitions.Partition.Range> ranges = Stream.ofAll(schema.getPartitions(indexCategory))
                .map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(id))
                .toJavaList();
        if(ranges.isEmpty())
            throw new FuseError.FuseErrorException(new FuseError("Index Schema routing error","No Index found for id "+id +" index category "+indexCategory ));

        return ranges.get(0).getIndices().iterator().next();
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
