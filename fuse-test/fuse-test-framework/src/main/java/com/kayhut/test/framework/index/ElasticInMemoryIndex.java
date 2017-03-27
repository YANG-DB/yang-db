package com.kayhut.test.framework.index;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import static com.kayhut.test.framework.TestUtil.deleteFolder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by moti on 3/19/2017.
 */
public class ElasticInMemoryIndex  implements AutoCloseable{
    //region Members
    private final int httpPort;
    private final int httpTransportPort;
    private final String esWorkingDir;
    private final String nodeName;
    private Node node;
    private List<TransportClient> transportClients = new LinkedList<>();
    private TransportClient client = null;
    //endregion

    //region Constructors
    public ElasticInMemoryIndex() {
        this("target/es", 9305, 9300, "fuse.test_elastic", new ElasticIndexConfigurer() {
            @Override
            public void configure(TransportClient client) {

            }
        });
    }

    public ElasticInMemoryIndex(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, ElasticIndexConfigurer configurer) {
        this.esWorkingDir = esWorkingDir;
        this.httpPort = httpPort;
        this.httpTransportPort = httpTransportPort;
        this.nodeName = nodeName;
        prepare();
        configure(configurer);
    }

    //endregion

    //region Methods
    public TransportClient getClient(){
        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", nodeName).build();
            TransportClient client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            transportClients.add(client);
            return client;
        } catch (UnknownHostException e) {
            throw new UnknownError(e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing");
        for(TransportClient client : transportClients){
            try{
                client.close();
            }catch(Exception ex){
                // do nothing
            }
        }
        node.close();
        deleteFolder(esWorkingDir + "\\" + nodeName);
    }

    private void configure(ElasticIndexConfigurer configurer) {
        configurer.configure(this.getClient());
    }

    private void prepare(){
        deleteFolder(esWorkingDir + "\\" + nodeName);
        Settings settings = Settings.builder()
                .put("path.home", esWorkingDir)
                .put("path.conf", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.work", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.tcp.port", httpTransportPort)
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0")
                .put("discovery.zen.ping.multicast.enabled", "false")
                .build();
        node = nodeBuilder().settings(settings).clusterName(nodeName).client(false).node();
        node = node.start();
    }
    //endregion
}
