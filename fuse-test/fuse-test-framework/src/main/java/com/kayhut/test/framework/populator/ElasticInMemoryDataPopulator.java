package com.kayhut.test.framework.populator;

import com.kayhut.test.framework.scenario.ScenarioDocument;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by moti on 3/12/2017.
 */
public class ElasticInMemoryDataPopulator extends BaseDataPopulator<TransportClient>{

    private static final int HTTP_PORT = 9205;
    private static final String HTTP_BASE_URL = "http://localhost";
    private static final int HTTP_TRANSPORT_PORT = 9305;
    private final String ES_WORKING_DIR = "target/es";
    private final String NODE_NAME = "fuse.test_elastic";
    private Node node;
    private List<TransportClient> transportClients = new LinkedList<>();
    private TransportClient client = null;

    public void prepare(){
        deleteFolder(ES_WORKING_DIR + "/" + NODE_NAME);
        Settings settings = Settings.builder()
                .put("path.home", ES_WORKING_DIR)
                .put("path.conf", ES_WORKING_DIR)
                .put("path.data", ES_WORKING_DIR)
                .put("path.work", ES_WORKING_DIR)
                .put("path.logs", ES_WORKING_DIR)
                .put("http.port", HTTP_PORT)
                .put("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0")
                .put("discovery.zen.ping.multicast.enabled", "false")
                .build();
        node = nodeBuilder().settings(settings).clusterName(NODE_NAME).client(false).node();
        node.start();
    }

    public TransportClient getClient(){
        try {
            TransportClient client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), HTTP_TRANSPORT_PORT));
            transportClients.add(client);
            return client;
        } catch (UnknownHostException e) {
            throw new UnknownError(e.getMessage());
        }
    }

    public void teardown(){
        node.close();
        deleteFolder(ES_WORKING_DIR + "/" + NODE_NAME);
        for(TransportClient client : transportClients){
            try{
                client.close();
            }catch(Exception ex){
                // do nothing
            }
        }
    }

    @Override
    public void indexDocument(ScenarioDocument doc) {
        if(client == null){
            client = getClient();
        }
        IndexResponse indexResponse = client.prepareIndex()
                .setId(doc.getId())
                .setIndex(doc.getIndexName())
                .setType(doc.getDocType())
                .setSource(doc.getDocValues())
                .execute()
                .actionGet();
        if(!indexResponse.isCreated()){
            throw new IllegalArgumentException("Inserting doc failed, docId = " + doc.getId());
        }
    }

    public static void deleteFolder(String folder) {
        File folderFile = new File(folder);
        File[] files = folderFile.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        folderFile.delete();
    }
}
