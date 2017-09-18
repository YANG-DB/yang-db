package com.kayhut.test.framework.index;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.kayhut.test.framework.TestUtil.deleteFolder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by moti on 3/19/2017.
 */
public class ElasticEmbeddedNode implements AutoCloseable {
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
                Settings settings = Settings.settingsBuilder()
                        .put("cluster.name", nodeName).build();
                this.client = TransportClient.builder().settings(settings).build()
                        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
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
        //System.clearProperty("mapper.allow_dots_in_name");
    }

    private void prepare() throws Exception {
        //System.setProperty("mapper.allow_dots_in_name", "true");
        this.close();
        Settings settings = Settings.builder()
                .put("path.home", esWorkingDir)
                .put("path.conf", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.work", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.tcp.port", httpTransportPort)
                .put("index.number_of_shards", numberOfShards)
                .put("index.number_of_replicas", 0)
                .put("discovery.zen.ping.multicast.enabled", "false")
                .build();
        node = nodeBuilder().settings(settings).clusterName(nodeName).client(false).node();
        node = node.start();
    }
    //endregion
}
