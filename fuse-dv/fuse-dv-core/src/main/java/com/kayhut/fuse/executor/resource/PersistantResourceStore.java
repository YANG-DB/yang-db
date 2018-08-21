package com.kayhut.fuse.executor.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.cursor.CreateCursorRequestDeserializer;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.transport.CreateQueryRequest.Type._stored;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class PersistantResourceStore implements ResourceStore {

    public static final String SYSTEM = "fuse_system";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TTL = "ttl";
    public static final String CREATION_TIME = "creationTime";
    public static final String QUERY = "query";
    public static final String ASG = "asg";
    public static final String REQUEST = "request";
    public static final String TYPE = "type";
    public static final String RESOURCE = "resource";
    private Client client;
    private ObjectMapper mapper;

    @Inject
    public PersistantResourceStore(Provider<Client> client,ObjectMapper mapper, Set<CompositeCursorFactory.Binding> cursorBindings) {
        this.client = client.get();
        this.mapper = mapper;
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class, new CreateCursorRequestDeserializer(cursorBindings));
        mapper.registerModules(module);
    }

    @Override
    public Collection<QueryResource> getQueryResources() {
        while (true) {
            try {
                final SearchRequestBuilder search = client.prepareSearch(SYSTEM);
                final SearchResponse response = search.setQuery(matchAllQuery()).get();
                return Arrays.asList(response.getHits().getHits()).stream()
                        .map(hit -> {
                            try {
                                return buildQueryResource(hit.getId(),hit.sourceAsMap());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (IndexNotFoundException e) {
                this.client.admin().indices()
                        .create(new CreateIndexRequest()
                                .waitForActiveShards(ActiveShardCount.ALL)
                                .index(SYSTEM)).actionGet();

            }
        }
    }

    private QueryResource buildQueryResource(String id, Map hit) throws IOException {
        final String name = hit.get(NAME).toString();
        final String ttl = hit.get(TTL).toString();
        final String creationTime = hit.get(CREATION_TIME).toString();
        final Query query = mapper.readValue(hit.getOrDefault(QUERY, "{}").toString(), Query.class);
        final AsgQuery asgQuery = mapper.readValue(hit.getOrDefault(ASG, "{}").toString(), AsgQuery.class);
        final CreateQueryRequest request = mapper.readValue(hit.getOrDefault(REQUEST,"{}").toString(),CreateQueryRequest.class);
        final QueryMetadata queryMetadata = new QueryMetadata(_stored, id, name, request.isSearchPlan(), Long.valueOf(creationTime), Long.valueOf(ttl));
        return new QueryResource(request, query, asgQuery, queryMetadata, PlanWithCost.EMPTY_PLAN);
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        try {
            final GetResponse response = client.prepareGet(SYSTEM, RESOURCE, queryId).get();
            if (response.isExists())
                return Optional.of(buildQueryResource(queryId, response.getSource()));
        } catch (IndexNotFoundException e) {
            final CreateIndexResponse response = this.client.admin().indices()
                    .create(new CreateIndexRequest()
                            .waitForActiveShards(ActiveShardCount.ALL)
                            .index(SYSTEM)).actionGet();
            if(response.isShardsAcked()) {
                if (client.prepareGet(SYSTEM, RESOURCE, queryId).get().isExists()) {
                    try {
                        return Optional.of(buildQueryResource(queryId, client.prepareGet(SYSTEM, RESOURCE, queryId).get().getSource()));
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        try {
            IndexResponse response = client.prepareIndex(SYSTEM, RESOURCE, queryResource.getQueryMetadata().getId())
                    .setSource(jsonBuilder()
                            .startObject()
                            .field(NAME, queryResource.getQueryMetadata().getName())
                            .field(TTL, queryResource.getQueryMetadata().getTtl())
                            .field(CREATION_TIME, queryResource.getQueryMetadata().getCreationTime())
                            .field(REQUEST, mapper.writeValueAsString(queryResource.getRequest()))
                            .field(QUERY, mapper.writeValueAsString(queryResource.getQuery()))
                            .field(ASG, mapper.writeValueAsString(queryResource.getAsgQuery()))
                            .endObject()
                    ).execute().actionGet();
            return response.status() == RestStatus.CREATED || response.status() == RestStatus.OK;
        } catch (IndexNotFoundException e) {
            this.client.admin().indices()
                    .create(new CreateIndexRequest()
                            .waitForActiveShards(ActiveShardCount.ALL)
                            .index(SYSTEM)).actionGet();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        final DeleteResponse response = client.prepareDelete(SYSTEM, RESOURCE, queryId).get();
        return response.status() == RestStatus.OK;
    }

    /**
     * ----------NOT-IMPLEMENTED-----NOT-IMPLEMENTED-----NOT-IMPLEMENTED--------------------------------------------
     **/
    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return Optional.empty();
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return Optional.empty();
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        return false;
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        return false;
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        return false;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        return false;
    }

    @Override
    public boolean test(CreateQueryRequest.Type type) {
        return type.equals(_stored);
    }
}
