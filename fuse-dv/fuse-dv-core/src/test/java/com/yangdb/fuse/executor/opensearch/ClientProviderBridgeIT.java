package com.yangdb.fuse.executor.opensearch;

import com.yangdb.fuse.executor.TestSuiteIndexProviderSuite;
import com.yangdb.fuse.opensearch.client.RestBridgeClient;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;
import org.junit.Assume;
import org.junit.Test;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The service's {@link ClientProvider} (non-mock) builds a REST bridge client; it must reach the suite's cluster over
 * HTTP and see the same data as the suite's own client. Runs inside {@link TestSuiteIndexProviderSuite}.
 */
public class ClientProviderBridgeIT {
    private static final String INDEX = "client_provider_bridge_it";

    private static OpensearchGraphConfiguration configuration(Integer httpPort) {
        OpensearchGraphConfiguration configuration = new OpensearchGraphConfiguration();
        configuration.setClusterHosts(new String[]{"localhost"});
        configuration.setClusterPort(ElasticEmbeddedNode.TRANSPORT_PORT);
        configuration.setClusterName(ElasticEmbeddedNode.FUSE_TEST_ELASTIC);
        if (httpPort != null) {
            configuration.setClusterHttpPort(httpPort);
        }
        return configuration;
    }

    @Test
    public void explicitHttpPort() {
        roundTrip(new ClientProvider(false, configuration(ElasticEmbeddedNode.HTTP_PORT)).get());
    }

    @Test
    public void legacyTransportPortMapsToHttpPort() {
        // the deprecated fallback (transport - 100) holds for the default ports and the agents' test ports
        Assume.assumeTrue(ElasticEmbeddedNode.TRANSPORT_PORT - 100 == ElasticEmbeddedNode.HTTP_PORT);
        roundTrip(new ClientProvider(false, configuration(null)).get());
    }

    private void roundTrip(Client bridge) {
        try {
            assertTrue(bridge instanceof RestBridgeClient);
            DocWriteResponse indexed = bridge.prepareIndex(INDEX).setId("1")
                    .setSource("{\"name\":\"Balerion\"}", XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).get();
            assertTrue(indexed.getResult() == DocWriteResponse.Result.CREATED || indexed.getResult() == DocWriteResponse.Result.UPDATED);

            SearchResponse viaBridge = bridge.prepareSearch(INDEX).setQuery(QueryBuilders.matchAllQuery()).get();
            assertEquals(1, viaBridge.getHits().getTotalHits().value);

            // the suite's own client sees the document the bridge wrote: same cluster
            Client suiteClient = TestSuiteIndexProviderSuite.client;
            assertEquals("Balerion", suiteClient.prepareGet(INDEX, "1").get().getSourceAsMap().get("name"));

            assertTrue(bridge.admin().indices().prepareDelete(INDEX).get().isAcknowledged());
        } finally {
            bridge.close();
        }
    }
}
