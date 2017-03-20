import com.kayhut.test.framework.index.ElasticInMemoryIndex;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import com.kayhut.test.framework.scenario.FileJsonDataProvider;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by moti on 3/19/2017.
 */
public class FolderScenarioProviderTests {
    private String docsFile = "src\\test\\resources\\IndexDocs\\docs.txt";


    @Test
    public void testDeserializeFolder() throws IOException {

        FileJsonDataProvider provider = new FileJsonDataProvider(docsFile);
        Stream<HashMap<String, Object>> scenarioDocuments = provider.getDocuments();
        List<HashMap<String, Object>> documents = scenarioDocuments.collect(Collectors.toList());
        Assert.assertEquals(2, documents.size());
    }

    @Test
    public void testSimpleElastic() throws Exception {
        String indexName = "index";
        String type = "docType";
        FileJsonDataProvider provider = new FileJsonDataProvider(docsFile);
        try (ElasticInMemoryIndex index = new ElasticInMemoryIndex()){
            TransportClient indexClient = index.getClient();
            ElasticDataPopulator populator = new ElasticDataPopulator(indexClient, indexName, type, "id", provider);
            populator.populate();
            indexClient.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
            SearchResponse searchResponse = indexClient.prepareSearch().execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            Assert.assertEquals(2,hits.getTotalHits());
        }
    }



}
