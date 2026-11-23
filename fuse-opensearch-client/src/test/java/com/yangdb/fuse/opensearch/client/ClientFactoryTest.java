package com.yangdb.fuse.opensearch.client;

import org.apache.http.HttpHost;
import org.junit.Test;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientFactoryTest {

    @Test
    public void explicitHttpPortWins() {
        assertEquals(9201, ClientFactory.resolveHttpPort(9201, 9300));
    }

    @Test
    public void transportPortMapsToHttpPortMinus100() {
        assertEquals(9200, ClientFactory.resolveHttpPort(null, 9300));
        assertEquals(9360, ClientFactory.resolveHttpPort(null, 9460));
        assertEquals(9200, ClientFactory.httpPortFromTransportPort(9300));
    }

    @Test
    public void noPortAtAllMeans9200() {
        assertEquals(ClientFactory.DEFAULT_HTTP_PORT, ClientFactory.resolveHttpPort(null, null));
    }

    @Test
    public void hostsParse() {
        assertEquals(new HttpHost("opensearch", 9200, "http"), ClientFactory.toHttpHost("opensearch", 9200));
        assertEquals(new HttpHost("10.0.0.1", 9201, "http"), ClientFactory.toHttpHost(" 10.0.0.1:9201 ", 9200));
        assertEquals(new HttpHost("os.example", 443, "https"), ClientFactory.toHttpHost("https://os.example:443", 9200));
        assertEquals(new HttpHost("os.example", 9200, "https"), ClientFactory.toHttpHost("https://os.example", 9200));
    }

    @Test
    public void remoteBuildsABridgeWithoutConnecting() {
        // like the TransportClient, construction is lazy: no cluster needs to be up
        try (Client client = ClientFactory.remote(Arrays.asList("localhost", "127.0.0.1:9201"), 9200,
                Settings.builder().put("cluster.name", "Dragons").build())) {
            assertTrue(client instanceof RestBridgeClient);
            assertEquals("Dragons", client.settings().get("cluster.name"));
            assertEquals(2, ((RestBridgeClient) client).restClient().getLowLevelClient().getNodes().size());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void noHostsIsAnError() {
        ClientFactory.remote(new String[0], 9200, Settings.EMPTY);
    }
}
