package index.test;

import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import com.kayhut.test.framework.providers.FileJsonDataProvider;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.scenario.DragonScenarioFolderElasticPopulator;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Map;

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
            TransportClient indexClient = index.getClient();
            ElasticDataPopulator populator = new ElasticDataPopulator(indexClient, indexName, type, "id", provider);
            populator.populate();
            indexClient.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
            SearchResponse searchResponse = indexClient.prepareSearch().execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            Assert.assertEquals(2,hits.getTotalHits());
        }
    }

    @Test
    public void testDragons() throws Exception {
        String indexName = "scenario_index";
        MappingFileElasticConfigurer configurer = new MappingFileElasticConfigurer(indexName, mappingFile);
        try (ElasticEmbeddedNode index = new ElasticEmbeddedNode(configurer)){
            TransportClient client = index.getClient();
            DragonScenarioFolderElasticPopulator populator = new DragonScenarioFolderElasticPopulator(client, loadFolder, indexName);
            populator.populate();
            client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
            SearchResponse dragonSearch = client.prepareSearch(indexName).setTypes("dragon").execute().actionGet();
            long totalHits = dragonSearch.getHits().getTotalHits();
            Assert.assertEquals(100, totalHits);

            SearchResponse personSearch = client.prepareSearch(indexName).setTypes("person").execute().actionGet();
            totalHits = personSearch.getHits().getTotalHits();
            Assert.assertEquals(321, totalHits);

            SearchResponse horseSearch = client.prepareSearch(indexName).setTypes("horse").execute().actionGet();
            totalHits = horseSearch.getHits().getTotalHits();
            Assert.assertEquals(100, totalHits);

            SearchResponse guildSearch = client.prepareSearch(indexName).setTypes("guild").execute().actionGet();
            totalHits = guildSearch.getHits().getTotalHits();
            Assert.assertEquals(60, totalHits);

            SearchResponse kingdomSearch = client.prepareSearch(indexName).setTypes("kingdom").execute().actionGet();
            totalHits = kingdomSearch.getHits().getTotalHits();
            Assert.assertEquals(10, totalHits);

            SearchResponse firesAtSearch = client.prepareSearch(indexName).setTypes("fires_at").execute().actionGet();
            totalHits = firesAtSearch.getHits().getTotalHits();
            Assert.assertEquals(2*5598, totalHits);

            SearchResponse freezesSearch = client.prepareSearch(indexName).setTypes("freezes").execute().actionGet();
            totalHits = freezesSearch.getHits().getTotalHits();
            Assert.assertEquals(2*5444, totalHits);

            SearchResponse originatesInSearch = client.prepareSearch(indexName).setTypes("originated_in").execute().actionGet();
            totalHits = originatesInSearch.getHits().getTotalHits();
            Assert.assertEquals(2*200, totalHits);

            SearchResponse registeredInSearch = client.prepareSearch(indexName).setTypes("registered_in").execute().actionGet();
            totalHits = registeredInSearch.getHits().getTotalHits();
            Assert.assertEquals(2*232, totalHits);

            SearchResponse parentSearch = client.prepareSearch(indexName).setTypes("parent").execute().actionGet();
            totalHits = parentSearch.getHits().getTotalHits();
            Assert.assertEquals(2*21, totalHits);

            SearchResponse subjectOfSearch = client.prepareSearch(indexName).setTypes("subject_of").execute().actionGet();
            totalHits = subjectOfSearch.getHits().getTotalHits();
            Assert.assertEquals(2*321, totalHits);

            SearchResponse knowsSearch = client.prepareSearch(indexName).setTypes("knows").execute().actionGet();
            totalHits = knowsSearch.getHits().getTotalHits();
            Assert.assertEquals(2*3668, totalHits);

            SearchResponse memberOfSearch = client.prepareSearch(indexName).setTypes("member_of").execute().actionGet();
            totalHits = memberOfSearch.getHits().getTotalHits();
            Assert.assertEquals(2*950, totalHits);

            SearchResponse ownsSearch = client.prepareSearch(indexName).setTypes("owns").execute().actionGet();
            totalHits = ownsSearch.getHits().getTotalHits();
            Assert.assertEquals(2*186, totalHits);
        }
    }

    @Test
    public void testHierarchy() throws Exception {
        String indexName = "scenario_index";
        MappingFileElasticConfigurer configurer = new MappingFileElasticConfigurer(indexName, mappingFile);
        try (ElasticEmbeddedNode index = new ElasticEmbeddedNode(configurer)) {
            TransportClient client = index.getClient();
            DragonScenarioFolderElasticPopulator populator = new DragonScenarioFolderElasticPopulator(client, loadFolder, indexName);
            populator.populate();
            client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
            SearchResponse firesAtSearch = client.prepareSearch(indexName).setTypes("fires_at").execute().actionGet();
            Map<String, Object> source = firesAtSearch.getHits().getAt(0).getSource();
            Assert.assertTrue(source.containsKey("entityA"));
            Assert.assertTrue(source.containsKey("entityB"));
            Assert.assertTrue(source.get("entityA") instanceof Map);
            Assert.assertTrue(((Map<String, Object>)source.get("entityA")).containsKey("id"));
            Assert.assertTrue(((Map<String, Object>)source.get("entityB")).containsKey("id"));
        }
    }
}
