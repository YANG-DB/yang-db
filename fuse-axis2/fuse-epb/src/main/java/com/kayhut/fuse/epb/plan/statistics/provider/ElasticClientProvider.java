package com.kayhut.fuse.epb.plan.statistics.provider;

import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
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
                esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), config.getStatTransportPort()));
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
