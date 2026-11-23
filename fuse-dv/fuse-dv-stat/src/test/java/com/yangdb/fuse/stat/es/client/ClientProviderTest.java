package com.yangdb.fuse.stat.es.client;

import com.yangdb.fuse.opensearch.client.RestBridgeClient;
import com.yangdb.fuse.stat.util.EsUtil;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.common.xcontent.XContentType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The statistics tool's clients are REST bridges: config-key mapping, and a round trip (including the cluster-state
 * based {@link EsUtil#isIndexExists}) against the embedded node over HTTP. Runs alone or inside StatTestSuite.
 */
public class ClientProviderTest {

    @BeforeClass
    public static void setup() throws Exception {
        GlobalElasticEmbeddedNode.getInstance();
    }

    private static Configuration config(Integer httpPort, Integer transportPort) {
        Configuration configuration = new BaseConfiguration();
        configuration.setProperty("es.cluster.name", ElasticEmbeddedNode.FUSE_TEST_ELASTIC);
        configuration.setProperty("es.nodes.hosts", "localhost");
        configuration.setProperty("statistics.cluster.name", ElasticEmbeddedNode.FUSE_TEST_ELASTIC);
        configuration.setProperty("statistics.nodes.hosts", "localhost");
        if (httpPort != null) {
            configuration.setProperty("es.client.http.port", httpPort);
            configuration.setProperty("statistics.client.http.port", httpPort);
        }
        if (transportPort != null) {
            configuration.setProperty("es.client.transport.port", transportPort);
            configuration.setProperty("statistics.client.transport.port", transportPort);
        }
        return configuration;
    }

    @Test
    public void portKeys() {
        assertEquals(9201, ClientProvider.httpPort(config(9201, 9300), "es.client.http.port", "es.client.transport.port"));
        assertEquals(9200, ClientProvider.httpPort(config(null, 9300), "es.client.http.port", "es.client.transport.port"));
        assertEquals(9360, ClientProvider.httpPort(config(null, 9460), "statistics.client.http.port", "statistics.client.transport.port"));
    }

    @Test
    public void dataAndStatClientsReachTheClusterOverHttp() throws Exception {
        Configuration configuration = config(ElasticEmbeddedNode.HTTP_PORT, ElasticEmbeddedNode.TRANSPORT_PORT);
        try (Client data = ClientProvider.getDataClient(configuration);
             Client stat = ClientProvider.getStatClient(configuration)) {
            assertTrue(data instanceof RestBridgeClient);
            assertTrue(stat instanceof RestBridgeClient);

            String index = "stat_client_provider_test";
            assertFalse(EsUtil.isIndexExists(stat, index));
            data.prepareIndex(index).setId("1").setSource("{\"name\":\"Meraxes\"}", XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).get();
            assertTrue("cluster state over the bridge", EsUtil.isIndexExists(stat, index));
            assertEquals(1, stat.prepareSearch(index).get().getHits().getTotalHits().value);
            assertTrue(data.admin().indices().prepareDelete(index).get().isAcknowledged());
        }
    }
}
