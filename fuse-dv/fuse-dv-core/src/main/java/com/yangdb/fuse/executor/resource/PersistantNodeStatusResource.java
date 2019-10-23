package com.yangdb.fuse.executor.resource;

/*-
 * #%L
 * fuse-dv-core
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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.resource.store.NodeStatusResource;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.yangdb.fuse.executor.ExecutorModule.globalClient;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class PersistantNodeStatusResource implements NodeStatusResource {

    public static final String SYSTEM = "fuse_node_info";
    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String UPDATE_TIME = "updateTime";
    public static final String RESOURCE = "resource";

    public static final String nodeName = "PersistantNodeStatusResource.@nodeName";

    private String name;
    private Client client;
    private MetricRegistry registry;

    @Inject
    public PersistantNodeStatusResource(@Named(globalClient) Client client,
                                        MetricRegistry registry) throws UnknownHostException {

        this.name = InetAddress.getLocalHost().getHostAddress();
        this.client = client;
        this.registry = registry;
    }


    @Override
    public Map<String, Object> getMetrics() {
        return getMetrics(name);
    }

    @Override
    public Map<String, Object> getMetrics(String node) {
        try {
            final GetResponse response = client.prepareGet(SYSTEM, RESOURCE, node).get();
            if (response.isExists())
                return Collections.emptyMap();
        } catch (IndexNotFoundException e) {
            final CreateIndexResponse response = this.client.admin().indices()
                    .create(new CreateIndexRequest()
                            .waitForActiveShards(ActiveShardCount.ALL)
                            .index(SYSTEM)).actionGet();
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean report() {
        try {
            IndexResponse response = client.prepareIndex(SYSTEM, RESOURCE, name)
                    .setSource(jsonBuilder()
                            .startObject()
                            .field(NODE, name)
                            .field(UPDATE_TIME, System.currentTimeMillis())
                            .field(DATA, logStatus())
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

    private Map<String, Object> logStatus() {
        Map<String, Object> stats = new HashMap<>();
        registry.getMetrics().forEach((key, value) -> {
            switch (key) {
                case "memory.total.used" :
                    stats.put(key, ((Gauge) value).getValue());
                    break;
                case "memory.heap.usage" :
                    stats.put(key, ((Gauge) value).getValue());
                    break;
                case "threads.count" :
                    stats.put(key, ((Gauge) value).getValue());
                    break;
                case "cursor.count" :
                    stats.put(key, ((Counter) value).getCount());
                    break;
            }
        });
        return stats;
    }

}
