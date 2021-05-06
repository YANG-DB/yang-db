package com.yangdb.fuse.executor.utils;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.typesafe.config.Config;
import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import javaslang.collection.Stream;

import java.util.List;

public abstract class ConfigUtils {

    public static ElasticGraphConfiguration createElasticGraphConfiguration(Config conf) {
        ElasticGraphConfiguration configuration = new ElasticGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(getStringList(conf, "elasticsearch.hosts")).toJavaArray(String.class));
        configuration.setClusterPort(conf.getInt("elasticsearch.port"));
        configuration.setClusterName(conf.getString("elasticsearch.cluster_name"));
        configuration.setElasticGraphDefaultSearchSize(conf.getLong("elasticsearch.default_search_size"));
        configuration.setElasticGraphMaxSearchSize(conf.getLong("elasticsearch.max_search_size"));
        configuration.setElasticGraphScrollSize(conf.getInt("elasticsearch.scroll_size"));
        configuration.setElasticGraphScrollTime(conf.getInt("elasticsearch.scroll_time"));
        // verify "elasticsearch.cluster" exists before using
        if(conf.hasPath("elasticsearch.cluster")) {
            configuration.setClusterProps(conf.getConfig("elasticsearch.cluster").entrySet());
        }

        configuration.setClientTransportIgnoreClusterName(conf.hasPath("client.transport.ignore_cluster_name") &&
                conf.getBoolean("client.transport.ignore_cluster_name"));

        return configuration;
    }

    public static List<String> getStringList(Config conf, String key) {
        try {
            return conf.getStringList(key);
        } catch (Exception ex) {
            String strList = conf.getString(key);
            return Stream.of(strList.split(",")).toJavaList();
        }
    }

}
