package com.kayhut.test.framework.populator;

import com.kayhut.test.framework.providers.GenericDataProvider;
import org.neo4j.graphdb.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by moti on 3/22/2017.
 */
public class Neo4jGenericEdgeDataPopulator implements DataPopulator{
    private GraphDatabaseService graphDatabaseService;
    private GenericDataProvider dataProvider;
    private String edgeLabel;
    private String sourceIdField;
    private String sourceLabel;
    private String destIdField;
    private String destLabel;
    private int BATCH_SIZE = 500;

    public Neo4jGenericEdgeDataPopulator(GraphDatabaseService graphDatabaseService,
                                         GenericDataProvider dataProvider,
                                         String EdgeLabel,
                                         String sourceIdField,
                                         String sourceLabel,
                                         String destIdField,
                                         String destLabel) {
        this.graphDatabaseService = graphDatabaseService;
        this.dataProvider = dataProvider;
        this.edgeLabel = EdgeLabel;
        this.sourceIdField = sourceIdField;
        this.sourceLabel = sourceLabel;
        this.destIdField = destIdField;
        this.destLabel = destLabel;
    }

    @Override
    public void populate() throws Exception {
        Iterable<Map<String, Object>> documents = dataProvider.getDocuments();
        Transaction transaction = graphDatabaseService.beginTx();
        int count = 0;
        for(Iterator<Map<String, Object>> iterator = documents.iterator(); iterator.hasNext();){
            Map<String, Object> doc = iterator.next();

            Object sourceId = doc.get(sourceIdField);
            Object destId = doc.get(destIdField);

            Node sourceNode = graphDatabaseService.findNode(Label.label(this.sourceLabel), this.sourceIdField, sourceId);
            Node destNode = graphDatabaseService.findNode(Label.label(this.destLabel), this.destIdField, destId);
            Relationship relationship = sourceNode.createRelationshipTo(destNode, RelationshipType.withName(this.edgeLabel));
            for(Map.Entry<String, Object> prop : doc.entrySet()){
                if(!(prop.getKey().equals(this.sourceIdField) || prop.getKey().equals(this.destIdField))){
                    relationship.setProperty(prop.getKey(), prop.getValue());
                }
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
