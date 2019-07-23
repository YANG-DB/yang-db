package com.yangdb.fuse.executor.resource;

/*-
 * #%L
 * fuse-dv-core
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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.resource.store.NodeStatusResource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class InMemNodeStatusResource implements NodeStatusResource {

    public static final String ID = "id";

    private String name;
    private MetricRegistry registry;

    @Inject
    public InMemNodeStatusResource(MetricRegistry registry) throws UnknownHostException {
        this.name = InetAddress.getLocalHost().getHostAddress();
        this.registry = registry;
    }


    @Override
    public Map<String, Object> getMetrics() {
        return getMetrics(name);
    }

    @Override
    public Map<String, Object> getMetrics(String node) {
        return Collections.unmodifiableMap(logStatus(registry));
    }

    @Override
    public boolean report() {
        return true;
    }

    private Map<String, Object> logStatus(MetricRegistry registry) {
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
