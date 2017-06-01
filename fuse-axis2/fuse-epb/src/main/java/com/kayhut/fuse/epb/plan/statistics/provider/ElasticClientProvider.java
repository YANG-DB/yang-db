package com.kayhut.fuse.epb.plan.statistics.provider;

import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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
        Settings settings = Settings.builder().put("client.transport.sniff", true).put("cluster.name", config.getStatClusterName()).build();
        TransportClient esClient = TransportClient.builder().settings(settings).build();
        for (String node : config.getStatNodesHosts()) {
            try {
                esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), config.getStatTransportPort()));
            } catch (UnknownHostException e) {
                throw new RuntimeException("Fatal Error: Unable to get host information");
            }
        }
        return esClient;
    }
    //endregion

    //region Fields
    private StatConfig config;
    //endregion

}
