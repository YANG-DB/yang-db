package com.yangdb.fuse.executor.utils;

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
        configuration.setClusterProps(conf.getConfig("elasticsearch.cluster").entrySet());

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
