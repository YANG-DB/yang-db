package com.kayhut.test.framework.populator;

import com.kayhut.test.framework.providers.GenericDataProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by moti on 3/22/2017.
 */
public class Neo4jGenericNodeDataPopulator implements DataPopulator{
    private GraphDatabaseService graphDatabaseService;
    private GenericDataProvider dataProvider;
    private String label;
    private int BATCH_SIZE = 500;

    public Neo4jGenericNodeDataPopulator(GraphDatabaseService graphDatabaseService, GenericDataProvider dataProvider, String label) {
        this.graphDatabaseService = graphDatabaseService;
        this.dataProvider = dataProvider;
        this.label = label;
    }

    @Override
    public void populate() throws Exception {
        Iterable<Map<String, Object>> documents = dataProvider.getDocuments();
        Transaction transaction = graphDatabaseService.beginTx();
        int count = 0;
        for(Iterator<Map<String, Object>> iterator = documents.iterator(); iterator.hasNext();){
            Map<String, Object> doc = iterator.next();
            Node node = graphDatabaseService.createNode(Label.label(this.label));
            for(Map.Entry<String, Object> prop : doc.entrySet()){
                node.setProperty(prop.getKey(), prop.getValue());
            }

            if(++count%BATCH_SIZE == 0){
                transaction.success();
                transaction.close();
                transaction = graphDatabaseService.beginTx();
            }
        }
        transaction.success();
        transaction.close();
    }
}
