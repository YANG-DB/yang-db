package com.kayhut.test.framework.index;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.File;

/**
 * Created by moti on 3/20/2017.
 */
public class Neo4jInMemoryIndex implements AutoCloseable {
    private GraphDatabaseService graphDatabaseService;

    public Neo4jInMemoryIndex(String folder) {
         this.graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder(new File(folder)).newGraphDatabase();
    }

    public GraphDatabaseService getClient(){
        return this.graphDatabaseService;
    }


    @Override
    public void close() throws Exception {
        this.graphDatabaseService.shutdown();
    }
}
