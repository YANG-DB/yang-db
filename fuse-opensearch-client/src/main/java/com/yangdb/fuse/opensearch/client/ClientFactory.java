package com.yangdb.fuse.opensearch.client;

/*-
 * #%L
 * fuse-opensearch-client
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

import org.apache.http.HttpHost;
import org.opensearch.client.Client;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Builds {@link Client}s for a <b>remote</b> OpenSearch cluster. They talk HTTP (the REST port, 9200 by default)
 * through a {@link RestBridgeClient}; see the module README for the transport-port to HTTP-port config mapping.
 */
public final class ClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);

    public static final int DEFAULT_HTTP_PORT = 9200;
    /** Deprecated fallback: http port = configured transport port - 100 (9300 -> 9200, test 9460 -> 9360). */
    public static final int TRANSPORT_TO_HTTP_OFFSET = 100;
    /** {@link Settings} key: socket (read) timeout of the REST client in milliseconds. */
    public static final String SOCKET_TIMEOUT_MS = "opensearch.http.socket_timeout_ms";
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 5 * 60 * 1000;
    /** {@link Settings} key: connect timeout of the REST client in milliseconds. */
    public static final String CONNECT_TIMEOUT_MS = "opensearch.http.connect_timeout_ms";
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;

    private static final AtomicBoolean transportPortWarned = new AtomicBoolean(false);

    private ClientFactory() {
    }

    /**
     * @param hosts    host names, {@code host:port} or {@code scheme://host:port}; a host without a port gets {@code httpPort}
     * @param httpPort the cluster's HTTP (REST) port
     * @param settings client settings ({@code cluster.name}, timeouts)
     */
    public static Client remote(Collection<String> hosts, int httpPort, Settings settings) {
        List<HttpHost> httpHosts = new ArrayList<>();
        for (String host : hosts) {
            httpHosts.add(toHttpHost(host, httpPort));
        }
        return remote(httpHosts, settings);
    }

    public static Client remote(String[] hosts, int httpPort, Settings settings) {
        return remote(Arrays.asList(hosts), httpPort, settings);
    }

    public static Client remote(List<HttpHost> hosts, Settings settings) {
        if (hosts.isEmpty()) {
            throw new IllegalArgumentException("no OpenSearch hosts configured");
        }
        int socketTimeout = settings.getAsInt(SOCKET_TIMEOUT_MS, DEFAULT_SOCKET_TIMEOUT_MS);
        int connectTimeout = settings.getAsInt(CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS);
        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]))
                .setRequestConfigCallback(config -> config
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout));
        logger.info("OpenSearch REST bridge client to {} (cluster.name={})", hosts, settings.get("cluster.name", ""));
        return new RestBridgeClient(new RestHighLevelClient(builder), settings);
    }

    /** Parses {@code host}, {@code host:port} or {@code scheme://host:port}. */
    public static HttpHost toHttpHost(String host, int defaultPort) {
        String value = host.trim();
        String scheme = "http";
        int schemeEnd = value.indexOf("://");
        if (schemeEnd >= 0) {
            scheme = value.substring(0, schemeEnd);
            value = value.substring(schemeEnd + 3);
        }
        int colon = value.lastIndexOf(':');
        if (colon > 0 && value.indexOf(':') == colon) {
            return new HttpHost(value.substring(0, colon), Integer.parseInt(value.substring(colon + 1)), scheme);
        }
        return new HttpHost(value, defaultPort, scheme);
    }

    /**
     * The HTTP port to use when a configuration names a port.
     *
     * @param httpPort      the explicit HTTP port key's value, or {@code null} if the key is absent
     * @param transportPort the legacy (transport) port key's value, or {@code null} if absent
     * @return {@code httpPort} if given; else {@code transportPort - 100} (deprecated, logs once); else 9200
     */
    public static int resolveHttpPort(Integer httpPort, Integer transportPort) {
        if (httpPort != null) {
            return httpPort;
        }
        if (transportPort != null) {
            return httpPortFromTransportPort(transportPort);
        }
        return DEFAULT_HTTP_PORT;
    }

    /** Deprecated mapping of a transport port (9300) to the matching HTTP port (9200). */
    public static int httpPortFromTransportPort(int transportPort) {
        int httpPort = transportPort - TRANSPORT_TO_HTTP_OFFSET;
        if (transportPortWarned.compareAndSet(false, true)) {
            logger.warn("DEPRECATED: OpenSearch is reached over HTTP now; the configured transport port {} is mapped to "
                    + "HTTP port {} (transport port - {}). Set the explicit http port key instead "
                    + "(e.g. opensearch.http.port); see fuse-opensearch-client/README.md",
                    transportPort, httpPort, TRANSPORT_TO_HTTP_OFFSET);
        }
        return httpPort;
    }
}
