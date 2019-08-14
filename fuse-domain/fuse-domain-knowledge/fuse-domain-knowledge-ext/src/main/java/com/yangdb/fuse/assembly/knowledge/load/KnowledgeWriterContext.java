package com.yangdb.fuse.assembly.knowledge.load;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.executor.ontology.schema.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchema.*;


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

    public KnowledgeContext getContext() {
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

    public static KnowledgeWriterContext init(Client client, RawSchema schema) {
        final KnowledgeWriterContext context = new KnowledgeWriterContext();
        context.client = client;
        context.schema = schema;
        context.created = new ArrayList<>();
        return context;
    }

    private static void populateBulk(BulkRequestBuilder bulk, RawSchema schema, String indexCategory, Client client, List<KnowledgeDomainBuilder> builders, ObjectMapper mapper) throws JsonProcessingException {
        for (KnowledgeDomainBuilder builder : builders) {
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(resolveIndexByLabelAndId(indexCategory,
                            builder.routing().orElseGet(builder::id),schema))
                    .setType(PGE)
                    .setId(builder.id())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(builder.toString(mapper), XContentType.JSON);
            builder.routing().ifPresent(request::setRouting);
            bulk.add(request);
        }
    }

    public static <T extends KnowledgeDomainBuilder> LoadResponse.CommitResponse commit(Client client, RawSchema schema, String indexCategory, ObjectMapper mapper, List<T> builders) throws JsonProcessingException {
        if(builders.isEmpty())
            return LoadResponse.CommitResponse.EMPTY;

        final BulkRequestBuilder bulk = client.prepareBulk();
        return process(client, schema, indexCategory, bulk, builders,mapper);
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
    public static LoadResponse<String, FuseError> commit(Client client, RawSchema schema, ObjectMapper mapper, KnowledgeContext context) throws JsonProcessingException {
        LoadResponse<String, FuseError> responses = new LoadResponseImpl();
        responses.response(commit(client,schema,ENTITY,mapper,context.getEntities()));
        responses.response(commit(client,schema,ENTITY,mapper,context.getRelationBuilders()));

        responses.response(commit(client,schema,ENTITY,mapper,context.geteValues()));
        responses.response(commit(client,schema,RELATION,mapper,context.getRelations()));
        responses.response(commit(client,schema,RELATION,mapper,context.getrValues()));
        //todo populate insight and references
        return responses;
    }

    private static <T extends KnowledgeDomainBuilder> Response process(Client client, RawSchema schema, String indexCategory, BulkRequestBuilder bulk, List<T> builders, ObjectMapper mapper) throws JsonProcessingException {
        Response response = new Response(indexCategory);
        populateBulk(bulk,schema, indexCategory,client, (List<KnowledgeDomainBuilder>) builders,mapper);
        builders.forEach(builder -> {
            try {
                populateBulk(bulk,schema, indexCategory,client,builder.additional(),mapper);
            } catch (JsonProcessingException e) {
                response.failure(new FuseError("commit build proccess failed",e));
            }
        });

        final BulkItemResponse[] items = bulk.get().getItems();
        for (BulkItemResponse item : items) {
            if (!item.isFailed()) {
                response.success(item.getId());
            }else {
                //log error
                BulkItemResponse.Failure failure = item.getFailure();
                DocWriteRequest<?> request = bulk.request().requests().get(item.getItemId());
                //todo - get TechId from request
                response.failure(new FuseError("commit failed",failure.toString()));
            }

        }
        Set<String> indices = Arrays.stream(bulk.get().getItems()).map(BulkItemResponse::getIndex).collect(Collectors.toSet());
        client.admin().indices().prepareRefresh(indices.toArray(new String[0])).get();
        return response;

    }

    private static String resolveIndexByLabelAndId(String indexCategory, String id, RawSchema schema) {
        return Stream.ofAll(schema.getPartitions(indexCategory))
                .map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(id))
                .map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoadResponseImpl implements LoadResponse<String,FuseError> {


        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<CommitResponse<String,FuseError>> responses;

        public LoadResponseImpl() {
            this.responses = new ArrayList<>();
        }

        public LoadResponse response(LoadResponse.CommitResponse<String,FuseError> response) {
            this.responses.add(response);
            return this;
        }

        @Override
        public List<CommitResponse<String,FuseError>> getResponses() {
            return responses;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements LoadResponse.CommitResponse<String,FuseError> {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<FuseError> failures;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> success;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String index;

        public Response() {
        }

        public Response(String index) {
            this.index = index;
            this.failures = new ArrayList<>();
            this.success = new ArrayList<>();
        }

        public Response failure(FuseError err) {
            failures.add(err);
            return this;
        }

        public Response failure(List<FuseError> failed) {
            this.failures.addAll(failed);
            return this;
        }

        public Response success(String itemId) {
            success.add(itemId);
            return this;
        }

        public Response success(List<String> itemIds) {
            success.addAll(itemIds);
            return this;
        }

        public List<FuseError> getFailures() {
            return failures;
        }

        public List<String> getSuccesses() {
            return success;
        }

        public String getIndex() {
            return index;
        }

    }
}
