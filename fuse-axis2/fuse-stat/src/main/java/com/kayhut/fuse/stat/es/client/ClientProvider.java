package com.kayhut.fuse.stat.es.client;

import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by benishue on 03-May-17.
 */
public class ClientProvider {

    private ClientProvider() {
        throw new IllegalAccessError("Utility class");
    }

    public static TransportClient getDataClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("es.cluster.name");
        int transportPort = configuration.getInt("es.client.transport.port");
        String[] hosts = configuration.getStringArray("es.nodes.hosts");

        return getTransportClient(clusterName, transportPort, hosts);
    }

    public static TransportClient getStatClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("statistics.cluster.name");
        int transportPort = configuration.getInt("statistics.client.transport.port");
        String[] hosts = configuration.getStringArray("statistics.nodes.hosts");

        return getTransportClient(clusterName, transportPort, hosts);
    }

    public static TransportClient getTransportClient(String clusterName, int transportPort, String[] hosts) throws UnknownHostException {
        Settings settings = Settings.builder().put("client.transport.sniff", true).put("cluster.name", clusterName).build();
        TransportClient esClient = TransportClient.builder().settings(settings).build();
        for(String node: hosts){
            esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), transportPort));
        }
        return esClient;
    }
}
