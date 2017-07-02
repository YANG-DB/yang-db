package index.test;

import com.kayhut.test.framework.index.Neo4jInMemoryIndex;
import com.kayhut.test.framework.populator.Neo4jGenericNodeDataPopulator;
import com.kayhut.test.framework.providers.FileJsonDataProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by moti on 3/22/2017.
 */
public class Neo4jGenericPopulationTests {
    private String dragonsFolder = new File(Paths.get("src", "test", "resources", "dragons").toString()).getAbsolutePath() + File.separator;
    private String neoFolder = Paths.get("target", "neo").toString();
    private Neo4jInMemoryIndex index;
    private GraphDatabaseService graphDatabaseService;
    private String docsFile = Paths.get("src", "test", "resources", "IndexDocs", "docs.txt").toString();

    @Before
    public void initialize(){
        this.index = new Neo4jInMemoryIndex(this.neoFolder);
        this.graphDatabaseService = index.getClient();
    }

    @After
    public void teardown() throws Exception {
        index.close();
    }

    @Test
    public void testLoadNodes() throws Exception {
        FileJsonDataProvider provider = new FileJsonDataProvider(docsFile);
        Neo4jGenericNodeDataPopulator populator = new Neo4jGenericNodeDataPopulator(graphDatabaseService, provider, "Temp");
        populator.populate();
        Transaction tx = graphDatabaseService.beginTx();
        ResourceIterable<Node> allNodes = graphDatabaseService.getAllNodes();
        Assert.assertEquals(2,allNodes.stream().count());
        tx.close();
    }




}
