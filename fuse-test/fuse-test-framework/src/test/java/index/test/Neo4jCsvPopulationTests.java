package index.test;

import com.kayhut.test.framework.DragonScenarioLoadConstants;
import com.kayhut.test.framework.index.Neo4jInMemoryIndex;
import com.kayhut.test.framework.populator.Neo4jCsvDataPopulator;
import com.kayhut.test.framework.providers.FileCsvDataProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by moti on 3/21/2017.
 */
public class Neo4jCsvPopulationTests {
    private String dragonsFolder = new File("src\\test\\resources\\dragons\\").getAbsolutePath().replace('\\','/') + "/";
    private String neoFolder = "src\\test\\resources\\neo";
    private Neo4jInMemoryIndex index;
    private GraphDatabaseService graphDatabaseService;

    @Before
    public void initialize(){
        this.index = new Neo4jInMemoryIndex(this.neoFolder);
        this.graphDatabaseService = index.getClient();
    }

    @After
    public void teardown() throws Exception {
        index.close();
    }

    private void loadFileToNeo(String fileName,String cypherTemplate, GraphDatabaseService graphDatabaseService) throws IOException {
        String cypherLoadCsv = String.format(cypherTemplate, getFilePath(fileName));
        Neo4jCsvDataPopulator populator = new Neo4jCsvDataPopulator(graphDatabaseService, cypherLoadCsv);
        populator.populate();
    }

    @Test
    public void testNeoLoadAll() throws IOException {
        loadFileToNeo("PeopleList.csv", DragonScenarioLoadConstants.LOAD_PERSONS_TEMPLATE,graphDatabaseService);
        loadFileToNeo("HorsesList.csv", DragonScenarioLoadConstants.LOAD_HORSES_TEMPLATE,graphDatabaseService);
        loadFileToNeo("GuildList.csv", DragonScenarioLoadConstants.LOAD_GUILDS_TEMPLATE, graphDatabaseService);
        loadFileToNeo("KingdomList.csv", DragonScenarioLoadConstants.LOAD_KINGDOMS_TEMPLATE,graphDatabaseService);
        loadFileToNeo("dragonsList.csv", DragonScenarioLoadConstants.LOAD_DRAGONS_TEMPLATE,graphDatabaseService);

        loadFileToNeo("DragonFiresAt.csv", DragonScenarioLoadConstants.LOAD_DRAGON_FIRES_TEMPLATE,graphDatabaseService);
        loadFileToNeo("DragonFreezes.csv", DragonScenarioLoadConstants.LOAD_DRAGON_FREEZES_TEMPLATE,graphDatabaseService);
        loadFileToNeo("DragonOriginatedInKingdom.csv", DragonScenarioLoadConstants.LOAD_DRAGON_ORIGINATED_IN_TEMPLATE,graphDatabaseService);
        loadFileToNeo("GuildRegisterInKingdom.csv", DragonScenarioLoadConstants.LOAD_GUILD_REGISTERED_IN_TEMPLATE,graphDatabaseService);
        loadFileToNeo("HorseOriginatedInKingdom.csv", DragonScenarioLoadConstants.LOAD_HORSE_ORIGINATED_IN_TEMPLATE,graphDatabaseService);
        loadFileToNeo("OffspringRelation.csv", DragonScenarioLoadConstants.LOAD_PERSON_OFFSPRING_TEMPLATE,graphDatabaseService);
        loadFileToNeo("PeopleSubjectOfKingdom.csv", DragonScenarioLoadConstants.LOAD_PERSON_SUBJECT_OF_TEMPLATE,graphDatabaseService);
        loadFileToNeo("PersonKnowsPerson.csv", DragonScenarioLoadConstants.LOAD_PERSON_KNOWS_TEMPLATE,graphDatabaseService);
        loadFileToNeo("PersonMemberOfGuild.csv", DragonScenarioLoadConstants.LOAD_PERSON_MEMBER_OF_TEMPLATE,graphDatabaseService);
        loadFileToNeo("PersonOwnsDregons.csv", DragonScenarioLoadConstants.LOAD_PERSON_OWNS_DRAGON_TEMPLATE,graphDatabaseService);
        loadFileToNeo("PersonOwnsHorses.csv", DragonScenarioLoadConstants.LOAD_PERSON_OWNS_HORSE_TEMPLATE,graphDatabaseService);
        Transaction transaction = graphDatabaseService.beginTx();
        List<Node> nodes = graphDatabaseService.getAllNodes().stream().collect(Collectors.toList());
        List<Relationship> relationships = graphDatabaseService.getAllRelationships().stream().collect(Collectors.toList());
        Assert.assertEquals(50, nodes.stream().filter(n -> n.hasLabel(Label.label("Person"))).count());
        Assert.assertEquals(100, nodes.stream().filter(n -> n.hasLabel(Label.label("Horse"))).count());
        Assert.assertEquals(50, nodes.stream().filter(n -> n.hasLabel(Label.label("Dragon"))).count());
        Assert.assertEquals(5, nodes.stream().filter(n -> n.hasLabel(Label.label("Guild"))).count());
        Assert.assertEquals(3, nodes.stream().filter(n -> n.hasLabel(Label.label("Kingdom"))).count());
        Assert.assertEquals(8988,relationships.stream().filter(r -> r.isType(RelationshipType.withName("FIRES_AT"))).count());
        Assert.assertEquals(9262,relationships.stream().filter(r -> r.isType(RelationshipType.withName("FREEZES"))).count());
        Assert.assertEquals(150,relationships.stream().filter(r -> r.isType(RelationshipType.withName("ORIGINATED_IN"))).count());
        Assert.assertEquals(5,relationships.stream().filter(r -> r.isType(RelationshipType.withName("REGISTERED_IN"))).count());
        Assert.assertEquals(0,relationships.stream().filter(r -> r.isType(RelationshipType.withName("PARENT"))).count());
        Assert.assertEquals(50,relationships.stream().filter(r -> r.isType(RelationshipType.withName("SUBJECT_OF"))).count());
        Assert.assertEquals(124,relationships.stream().filter(r -> r.isType(RelationshipType.withName("KNOWS"))).count());
        Assert.assertEquals(49,relationships.stream().filter(r -> r.isType(RelationshipType.withName("MEMBER_OF"))).count());
        Assert.assertEquals(49+33,relationships.stream().filter(r -> r.isType(RelationshipType.withName("OWNS"))).count());
    }

    private String getFilePath(String fileName) {
        return "file:///" +dragonsFolder+  fileName;
    }
}
