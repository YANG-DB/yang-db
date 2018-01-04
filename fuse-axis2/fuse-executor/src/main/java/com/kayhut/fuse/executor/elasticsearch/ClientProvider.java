package com.kayhut.fuse.executor.elasticsearch;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.kayhut.fuse.executor.elasticsearch.logging.LoggingClient;
import com.kayhut.fuse.executor.mock.elasticsearch.MockClient;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by roman.margolis on 04/01/2018.
 */
public class ClientProvider implements Provider<Client> {
    @Inject
    //region Constructors
    public ClientProvider(Config conf, MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.conf = conf;
    }
    //endregion

    //region Provider Implementation
    @Override
    public Client get() {
        if (this.conf.hasPath("fuse.elasticsearch.mock")) {
            boolean clientMock = this.conf.getBoolean("fuse.elasticsearch.mock");
            if (clientMock) {
                System.out.println("Using mock elasticsearch client!");
                return new MockClient();
            }
        }

        ElasticGraphConfiguration configuration = createElasticGraphConfiguration(this.conf);

        Settings settings = Settings.builder().put("cluster.name", configuration.getClusterName()).build();
        TransportClient client = new PreBuiltTransportClient(settings);
        Stream.of(configuration.getClusterHosts()).forEach(host -> {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), configuration.getClusterPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return new LoggingClient(client, this.metricRegistry);
    }
    //endregion

    //region Private Methods
    private ElasticGraphConfiguration createElasticGraphConfiguration(Config conf) {
        ElasticGraphConfiguration configuration = new ElasticGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(conf.getStringList("elasticsearch.hosts")).toJavaArray(String.class));
        configuration.setClusterPort(conf.getInt("elasticsearch.port"));
        configuration.setClusterName(conf.getString("elasticsearch.cluster_name"));
        configuration.setElasticGraphDefaultSearchSize(conf.getLong("elasticsearch.default_search_size"));
        configuration.setElasticGraphMaxSearchSize(conf.getLong("elasticsearch.max_search_size"));
        configuration.setElasticGraphScrollSize(conf.getInt("elasticsearch.scroll_size"));
        configuration.setElasticGraphScrollTime(conf.getInt("elasticsearch.scroll_time"));
        return configuration;
    }
    //endregion

    //region Fields
    private MetricRegistry metricRegistry;
    private Config conf;
    //endregion
}
