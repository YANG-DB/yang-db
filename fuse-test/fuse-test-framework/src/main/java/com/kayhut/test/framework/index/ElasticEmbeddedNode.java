package com.kayhut.test.framework.index;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;

import static com.kayhut.test.framework.TestUtil.deleteFolder;

/**
 * Created by moti on 3/19/2017.
 */
public class ElasticEmbeddedNode implements AutoCloseable {
    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins);
        }
    }
    //endregion

    //region Members
    private final int httpPort;
    private final int httpTransportPort;
    private final String esWorkingDir;
    private final String nodeName;
    private final int numberOfShards;
    private Node node;
    private TransportClient client = null;
    //endregion

    //region Constructors
    public ElasticEmbeddedNode(String clusterName) throws Exception {
        this("target/es", 9200, 9300, clusterName);
    }

    public ElasticEmbeddedNode(String clusterName, int numberOfShards) throws Exception {
        this("target/es", 9200, 9300, clusterName, numberOfShards);
    }

    public ElasticEmbeddedNode() throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic");
    }

    public ElasticEmbeddedNode(ElasticIndexConfigurer... configurers) throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic", configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, ElasticIndexConfigurer... configurers) throws Exception {
        this(esWorkingDir, httpPort, httpTransportPort, nodeName, 1, configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, ElasticIndexConfigurer... configurers) throws Exception {
        this.esWorkingDir = esWorkingDir;
        this.httpPort = httpPort;
        this.httpTransportPort = httpTransportPort;
        this.nodeName = nodeName;
        this.numberOfShards = numberOfShards;
        prepare();

        for (ElasticIndexConfigurer configurer : configurers) {
            configurer.configure(this.getClient());
        }
    }

    //endregion

    //region Methods
    public TransportClient getClient() {
        if (this.client == null) {
            try {
                Settings settings = Settings.builder().put("cluster.name", nodeName).build();
                this.client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            } catch (UnknownHostException e) {
                throw new UnknownError(e.getMessage());
            }
        }

        return this.client;
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing");
        if (this.client != null) {
            try {
                this.client.close();
                this.client = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (this.node != null) {
            this.node.close();
            this.node = null;
        }

        deleteFolder(esWorkingDir);
    }

    private void prepare() throws Exception {
        this.close();

        Settings settings = Settings.builder()
                .put("cluster.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.enabled", "true")
                .put("transport.tcp.port", httpTransportPort)
                .build();

        this.node = new PluginConfigurableNode(settings, Collections.singletonList(Netty4Plugin.class));
        this.node = this.node.start();
    }
    //endregion
}
