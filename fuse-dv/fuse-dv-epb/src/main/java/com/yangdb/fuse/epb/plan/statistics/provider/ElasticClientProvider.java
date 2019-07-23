package com.yangdb.fuse.epb.plan.statistics.provider;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.epb.plan.statistics.configuration.StatConfig;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by benishue on 24-May-17.
 */
public class ElasticClientProvider {

    //region Ctrs
    public ElasticClientProvider(StatConfig config) {
        this.config = config;
    }
    //endregion

    //region Public Methods
    public TransportClient getStatClient() {
        Settings settings = Settings.builder().put("cluster.name", config.getStatClusterName()).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        for (String node : config.getStatNodesHosts()) {
            try {
                esClient.addTransportAddress(new TransportAddress(InetAddress.getByName(node), config.getStatTransportPort()));
            } catch (UnknownHostException e) {
                throw new RuntimeException("Fatal Error: Unable getTo get host information");
            }
        }
        return esClient;
    }
    //endregion

    //region Fields
    private final StatConfig config;
    //endregion

}
