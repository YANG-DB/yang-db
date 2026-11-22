package com.yangdb.fuse.stat.es.client;

/*-
 * #%L
 * fuse-dv-stat
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

import com.yangdb.fuse.opensearch.client.ClientFactory;
import org.apache.commons.configuration.Configuration;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;

import java.net.UnknownHostException;

/**
 * Created by benishue on 03-May-17.
 * <p>
 * Clients are REST bridges (fuse-opensearch-client) on the clusters' HTTP ports: {@code es.client.http.port} /
 * {@code statistics.client.http.port}, else (deprecated) the {@code *.client.transport.port} - 100.
 */
public class ClientProvider {

    private ClientProvider() {
        throw new IllegalAccessError("Utility class");
    }

    //region Static Methods
    public static Client getDataClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("es.cluster.name");
        int httpPort = httpPort(configuration, "es.client.http.port", "es.client.transport.port");
        String[] hosts = configuration.getStringArray("es.nodes.hosts");

        return getClient(clusterName, httpPort, hosts);
    }

    public static Client getStatClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("statistics.cluster.name");
        int httpPort = httpPort(configuration, "statistics.client.http.port", "statistics.client.transport.port");
        String[] hosts = configuration.getStringArray("statistics.nodes.hosts");

        return getClient(clusterName, httpPort, hosts);
    }

    public static Client getClient(String clusterName, int httpPort, String[] hosts) {
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        return ClientFactory.remote(hosts, httpPort, settings);
    }

    static int httpPort(Configuration configuration, String httpPortKey, String transportPortKey) {
        return ClientFactory.resolveHttpPort(
                configuration.containsKey(httpPortKey) ? configuration.getInt(httpPortKey) : null,
                configuration.containsKey(transportPortKey) ? configuration.getInt(transportPortKey) : null);
    }
    //endregion
}
