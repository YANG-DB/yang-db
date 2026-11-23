package com.yangdb.fuse.executor.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.SystemResourceKind;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.test.BaseITMarker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.client;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.StorageType._stored;

/**
 * The stored-query system index ({@code fuse_system}) on an untyped (OpenSearch 2.x) cluster: query documents carry
 * {@link SystemResourceKind#FIELD} = {@link SystemResourceKind#QUERY}, reads only return that kind, and documents
 * written by a 1.3-era YANG-DB (no kind field; they had the mapping type "resource") are still read.
 */
public class PersistentResourceStoreIT implements BaseITMarker {

    private PersistentResourceStore store;

    @Before
    public void setUp() {
        dropSystemIndex();
        store = new PersistentResourceStore(() -> client, new ObjectMapper(), Collections.emptySet());
    }

    @After
    public void tearDown() {
        dropSystemIndex();
    }

    @Test
    public void storedQueryRoundTripsWithKind() {
        //the first call creates the system index (with the kind mapping)
        Assert.assertTrue(store.getQueryResources().isEmpty());
        Assert.assertTrue(store.addQueryResource(queryResource("q1", "first")));
        refresh();

        Map<String, Object> source = client.prepareGet(PersistentResourceStore.SYSTEM, "q1").get().getSource();
        Assert.assertEquals(SystemResourceKind.QUERY, source.get(SystemResourceKind.FIELD));

        Optional<QueryResource> found = store.getQueryResource("q1");
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("first", found.get().getQueryMetadata().getName());
        Assert.assertEquals(1, store.getQueryResources().size());

        Assert.assertTrue(store.deleteQueryResource("q1"));
        refresh();
        Assert.assertFalse(store.getQueryResource("q1").isPresent());
    }

    @Test
    public void otherKindsAreIgnored() {
        store.getQueryResources();
        Assert.assertTrue(store.addQueryResource(queryResource("q1", "first")));
        Map<String, Object> other = new HashMap<>();
        other.put(SystemResourceKind.FIELD, SystemResourceKind.NODE);
        other.put(PersistentResourceStore.NAME, "not a query");
        client.prepareIndex(PersistentResourceStore.SYSTEM).setId("n1").setSource(other).get();
        refresh();

        Assert.assertEquals(1, store.getQueryResources().size());
        Assert.assertFalse(store.getQueryResource("n1").isPresent());
        Assert.assertFalse("a document of another kind is not deleted", store.deleteQueryResource("n1"));
        Assert.assertTrue(client.prepareGet(PersistentResourceStore.SYSTEM, "n1").get().isExists());
    }

    @Test
    public void legacyDocumentWithoutKindIsStillRead() throws Exception {
        store.getQueryResources();
        ObjectMapper mapper = new ObjectMapper();
        QueryResource resource = queryResource("legacy", "old");
        Map<String, Object> legacy = new HashMap<>();
        legacy.put(PersistentResourceStore.NAME, "old");
        legacy.put(PersistentResourceStore.TTL, 1000L);
        legacy.put(PersistentResourceStore.CREATION_TIME, 1L);
        legacy.put(PersistentResourceStore.REQUEST, mapper.writeValueAsString(resource.getRequest()));
        legacy.put(PersistentResourceStore.QUERY, mapper.writeValueAsString(resource.getQuery()));
        legacy.put(PersistentResourceStore.ASG, "null");
        client.prepareIndex(PersistentResourceStore.SYSTEM).setId("legacy").setSource(legacy).get();
        refresh();

        Assert.assertTrue(store.getQueryResource("legacy").isPresent());
        Assert.assertEquals(1, store.getQueryResources().size());
    }

    private static QueryResource queryResource(String id, String name) {
        Query query = Query.Builder.instance().withName(name).withOnt("Dragons").build();
        CreateQueryRequest request = new CreateQueryRequest(id, name, query);
        QueryMetadata metadata = new QueryMetadata(_stored, id, name, false, System.currentTimeMillis(), 1000L);
        return new QueryResource(request, query, null, metadata, PlanWithCost.EMPTY_PLAN);
    }

    private static void refresh() {
        client.admin().indices().prepareRefresh(PersistentResourceStore.SYSTEM).get();
    }

    private static void dropSystemIndex() {
        if (client.admin().indices().exists(new IndicesExistsRequest(PersistentResourceStore.SYSTEM)).actionGet().isExists()) {
            client.admin().indices().delete(new DeleteIndexRequest(PersistentResourceStore.SYSTEM)).actionGet();
        }
    }
}
