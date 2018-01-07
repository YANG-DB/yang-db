package com.kayhut.test.framework.index;

/*import com.kayhut.test.framework.TestUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.File;*/

/**
 * Created by moti on 3/20/2017.
 */
/*public class Neo4jInMemoryIndex implements AutoCloseable {
    private GraphDatabaseService graphDatabaseService;
    private boolean isTest;
    private String folder;

    public Neo4jInMemoryIndex(String folder) {
        this(folder, true);
    }

    public Neo4jInMemoryIndex(String folder, boolean isTest) {
        TestUtil.deleteFolder(folder);
        if(isTest) {
            this.graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder(new File(folder)).newGraphDatabase();
        }
        else {
            this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File(folder));
        }
        this.folder = folder;
        this.isTest = isTest;
    }

    public GraphDatabaseService getClient(){
        return this.graphDatabaseService;
    }


    @Override
    public void close() throws Exception {
        this.graphDatabaseService.shutdown();
        if(!this.isTest){
            TestUtil.deleteFolder(this.folder);
        }
    }
}*/
