package index.test;

import com.yangdb.fuse.client.elastic.BaseFuseElasticClient;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.populator.ElasticDataPopulator;
import com.yangdb.fuse.test.framework.providers.FileJsonDataProvider;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * Created by moti on 3/27/2017.
 */
public class ElasticIndexTests {
    private String docsFile = Paths.get("src", "test", "resources", "IndexDocs", "docs.txt").toString();
    private String loadFolder = Paths.get("src", "test", "resources", "dragons_dual").toString();
    private String mappingFile =  Paths.get("src", "test", "resources", "dragon_mapping.json").toString();

    @Test
    public void testSimpleElastic() throws Exception {
        String indexName = "index";
        String type = "docType";
        FileJsonDataProvider provider = new FileJsonDataProvider(docsFile);
        try (ElasticEmbeddedNode index = new ElasticEmbeddedNode()){
            BaseFuseElasticClient indexClient = index.getClient();
            ElasticDataPopulator populator = new ElasticDataPopulator(indexClient, indexName, type, "id", provider);
            populator.populate();
            indexClient.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
            SearchResponse searchResponse = indexClient.prepareSearch().execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            Assert.assertEquals(2,hits.getTotalHits().value);
        }
    }

}
