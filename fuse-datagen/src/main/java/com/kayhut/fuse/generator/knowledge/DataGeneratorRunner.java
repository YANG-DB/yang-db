package com.kayhut.fuse.generator.knowledge;

import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DataGeneratorRunner {
    public static void main(String[] args) {
        String esHost = args[0];

        String fromContext = args[1];
        String toContext = args[2];

        double scaleFactor = Double.parseDouble(args[3]);
        double entityOverlapFactor = Double.parseDouble(args[4]);
        double entityValueOverlapFactor = Double.parseDouble(args[5]);

    }

    public static void run(
            String esHost,
            String fromContext,
            String toContext,
            double scaleFactor,
            double entityOverlapFactor,
            double entityValueOverlapFactor) throws UnknownHostException {

        Client client = getClient(esHost);
    }

    private static Client getClient(String esHost) throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "whatever")
                .put("client.transport.ignore_cluster_name", true)
                .build();

        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), 9300));
        return client;
    }
}
