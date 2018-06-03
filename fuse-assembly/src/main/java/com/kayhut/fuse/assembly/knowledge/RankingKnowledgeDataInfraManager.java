package com.kayhut.fuse.assembly.knowledge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.assembly.knowlegde.KnowledgeReference;
import com.kayhut.fuse.assembly.knowlegde.Reference;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import javaslang.collection.Stream;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by moti
 */
public class RankingKnowledgeDataInfraManager {
    private static final Logger logger = LoggerFactory.getLogger(RankingKnowledgeDataInfraManager.class);

    private TransportClient client;
    private SimpleDateFormat sdf;
    private Config conf;
    private RawSchema schema;

    public RankingKnowledgeDataInfraManager(String confPath) throws UnknownHostException {
        try {
            File configFile = new File(confPath);
            this.conf = ConfigFactory.parseFileAnySyntax(configFile, ConfigParseOptions.defaults().setAllowMissing(false));
            this.schema = ((Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_raw_schema"))).newInstance();

            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        } catch (Exception exc) {

        }
    }

    public void client_connect() {
        Settings settings = Settings.builder().put("cluster.name", conf.getConfig("elasticsearch").getString("cluster_name")).build();
        int port = conf.getConfig("elasticsearch").getInt("port");
        client = new PreBuiltTransportClient(settings);
        conf.getConfig("elasticsearch").getList("hosts").unwrapped().forEach(host -> {
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host.toString()), port));
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    public void client_close() {
        client.close();
    }

    public long init() throws IOException {
        String workingDir = System.getProperty("user.dir");
        File templates = Paths.get(workingDir,"resources", "assembly", "Knowledge", "indexTemplates").toFile();

        File[] templateFiles = templates.listFiles();
        if (templateFiles != null) {
            for (File templateFile : templateFiles) {
                String templateName = FilenameUtils.getBaseName(templateFile.getName());
                String template = FileUtils.readFileToString(templateFile, "utf-8");
                if (!client.admin().indices().getTemplates(new GetIndexTemplatesRequest(templateName)).actionGet().getIndexTemplates().isEmpty()) {
                    client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet();
                }
                client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template, XContentType.JSON)).actionGet();
            }
        }

        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
    }

    public long drop() throws IOException {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    List<String> _references;
    ObjectMapper _mapper;

    private void PrepareReferenceValues() throws JsonProcessingException {
        _references = new ArrayList<String>();

        List<String> auth = Arrays.asList("source1.procedure1", "source1.procedure1");
        KnowledgeReference ref = new KnowledgeReference();
        Reference reference = ref.getRef();
        reference.setAuthorization(auth);
        reference.setSystem("system7");
        reference.setCreationUser("Arla Nava");
        reference.setAuthorizationCount(1);
        reference.setLastUpdateUser("Lucia Mark");
        reference.setType("reference");
        reference.setUrl("http://1.1.1.1:6200/clown");
        reference.setTitle("Sample Title 1");
        try {
            reference.setCreationTime(sdf.parse("2018-04-26 14:02:40.533"));
            reference.setLastUpdateTime(sdf.parse("2018-04-29 13:02:40.133"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        _references.add(_mapper.writeValueAsString(ref));

        ref = new KnowledgeReference();
        reference = ref.getRef();
        reference.setAuthorization(auth);
        reference.setSystem("system7");
        reference.setCreationUser("Haim Moshe");
        reference.setAuthorizationCount(1);
        reference.setLastUpdateUser("Mark Rucinovitch");
        reference.setType("reference");
        reference.setUrl("http://1.1.1.1:6200/circus");
        reference.setTitle("Sample Title 2");
        try {
            reference.setCreationTime(sdf.parse("2018-04-24 14:02:40.533"));
            reference.setLastUpdateTime(sdf.parse("2018-04-27 13:02:40.133"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        _references.add(_mapper.writeValueAsString(ref));
    }

    private final String cEntity = "entity";
    private List<String> _entities;
    private int numPersons = 20;

    private void PrepareEntityValues() throws JsonProcessingException {
        _entities = new ArrayList<>();
        for (int i = 1; i <= numPersons; i++) {
            ObjectNode on = createEntityObject(getEntityId(i),
                    "context1",
                    "person",
                    1,
                    "User",
                    "User",
                    "2018-03-24 14:02:40.533",
                    "2018-03-24 14:02:40.533");
            _entities.add(_mapper.writeValueAsString(on));
            on = createEntityObject(getEntityId(i),
                    "global",
                    "person",
                    1,
                    "User",
                    "User",
                    "2018-03-24 14:02:40.533",
                    "2018-03-24 14:02:40.533");
            _entities.add(_mapper.writeValueAsString(on));
        }
    }

    private String getEntityId(int id) {
        return "e" + String.format(schema.getIdFormat(cEntity), id);
    }

    private ObjectNode createEntityObject(String logicalId,
                                          String context,
                                          String category,
                                          int authCount,
                                          String lastUpdateUser,
                                          String creationUser,
                                          String lastUpdateTime,
                                          String creationTime ) {
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cEntity);
        on.put("logicalId", logicalId);
        on.put("context", context);
        on.put("category", category);
        on.put("authorizationCount", authCount);
        on.put("lastUpdateUser", lastUpdateUser);
        on.put("creationUser", creationUser);
        on.put("lastUpdateTime", lastUpdateTime);
        on.put("creationTime", creationTime);

        ArrayNode refsNode = _mapper.createArrayNode();
        refsNode.add(getReferenceId(1));
        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.set("authorization", authNode);
        on.set("refs", refsNode);
        return on;
    }

    private String getReferenceId(int id) {
        return "ref" + String.format(schema.getIdFormat("reference"), id);
    }

    private final String cEntityValue = "e.value";
    private List<String> _entitiesValues;
    private List<Integer>  _evEntityId;

    private void PrepareEntityValuesValues() throws JsonProcessingException {
        _entitiesValues = new ArrayList<>();
        _evEntityId = new ArrayList<>();
        String context = "global";


        List<List<String>> nicks = Arrays.asList(
                Arrays.asList("moti", "moti cohen"),
                Arrays.asList("moti"),
                Arrays.asList("moti cohen", "roman"),
                Arrays.asList("motic"),
                Arrays.asList("roman"),
                Arrays.asList("tonette kwon"),
                Arrays.asList("veorgiana vanasse"),
                Arrays.asList("veorgiana vanasse suzette"),
                Arrays.asList("suzette veorgiana vanasse"),
                Arrays.asList("aaa bbb eee ccc"),
                Arrays.asList("aaa bbb cccd"),
                Arrays.asList("laaa bbb cccd"),
                Arrays.asList("aaa ccc"),
                Arrays.asList("aaa ccc bbb"),
                Arrays.asList("laaa cccd")

        );
        List<List<String>> titles = Arrays.asList(
                Arrays.asList("moti cohen"),
                Arrays.asList("moti"),
                Arrays.asList("roman"),
                Arrays.asList("motic"),
                Arrays.asList("roman"),
                Arrays.asList("tonette kwon"),
                Arrays.asList("veorgiana vanasse"),
                Arrays.asList("veorgiana vanasse suzette"),
                Arrays.asList("suzette veorgiana vanasse"),
                Arrays.asList("aaa bbb eee ccc"),
                Arrays.asList("aaa bbb cccd"),
                Arrays.asList("laaa bbb cccd"),
                Arrays.asList("aaa ccc"),
                Arrays.asList("aaa ccc bbb"),
                Arrays.asList("laaa cccd")


        );
        for (int i = 1 ; i <= nicks.size(); i++) {

            int finalI = i;
            nicks.get(i-1).forEach(nick -> {
                _evEntityId.add(finalI);
                ObjectNode on = createEntityValueObject(getEntityId(finalI) + "." + context,
                        context,
                        1,
                        "nicknames",
                        "nickname",
                        "[\"\"]",
                        "User",
                        "2018-03-24 14:02:40.533",
                        "User",
                        "2018-03-24 14:02:40.533",
                        nick
                );
                try {
                    _entitiesValues.add(_mapper.writeValueAsString(on));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            titles.get(i-1).forEach(title -> {
                _evEntityId.add(finalI);
                ObjectNode on = createEntityValueObject(getEntityId(finalI) + "." + context,
                        context,
                        1,
                        "title",
                        "title",
                        "[\"\"]",
                        "User",
                        "2018-03-24 14:02:40.533",
                        "User",
                        "2018-03-24 14:02:40.533",
                        title
                );
                try {
                    _entitiesValues.add(_mapper.writeValueAsString(on));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    private ObjectNode createEntityValueObject(String entityId,
                                               String context,
                                               int authCount,
                                               String fieldId,
                                               String bdt,
                                               String refs,
                                               String lastUpdateUser,
                                               String lastUpdateTime,
                                               String creationUser,
                                               String creationTime,
                                               String stringValue) {
        ObjectNode on  = createEntityValueObject(entityId, context, authCount, fieldId, bdt, refs, lastUpdateUser, lastUpdateTime, creationUser, creationTime);
        on.put("stringValue", stringValue);
        return on;
    }

    private ObjectNode createEntityValueObject(String entityId,
                                               String context,
                                               int authCount,
                                               String fieldId,
                                               String bdt,
                                               String refs,
                                               String lastUpdateUser,
                                               String lastUpdateTime,
                                               String creationUser,
                                               String creationTime) {
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cEntityValue);
        on.put("entityId", entityId);
        on.put("context", context);
        on.put("authorizationCount", authCount);
        on.put("fieldId", fieldId);
        on.put("bdt", bdt);
        on.put("refs", refs);
        on.put("lastUpdateUser", lastUpdateUser);
        on.put("lastUpdateTime", lastUpdateTime);
        on.put("creationUser", creationUser);
        on.put("creationTime", creationTime);

        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);
        return on;
    }

    private final String cInsight = "insight";
    private List<String> _insights;

    private void PrepareInsights() throws JsonProcessingException {
        _insights = new ArrayList<>();
        int id = 1;
        String insightId = "i" + String.format(schema.getIdFormat("insight"), id);

        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cInsight);
        on.put("content", "Very important, superb insight");
        on.put("context", "context1");

        ArrayNode entityIds = _mapper.createArrayNode();
        entityIds.add("e" + String.format(schema.getIdFormat("entity"), 1));

        on.put("entityIds", entityIds);

        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        ArrayNode refsNode = _mapper.createArrayNode();
        refsNode.add("ref" + String.format(schema.getIdFormat("reference"), 1));

        on.put("refs", refsNode);
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Michal");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationUser", "Hana");
        on.put("creationTime", sdf.format(new Date(System.currentTimeMillis())));

        _insights.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", cInsight);
        on.put("content", "Quite poor, and mostly unuseful insight just tobe tested");
        on.put("context", "context1");

        entityIds = _mapper.createArrayNode();
        entityIds.add("e" + String.format(schema.getIdFormat("entity"), 1));
        entityIds.add("e" + String.format(schema.getIdFormat("entity"), 2));

        on.put("entityIds", entityIds);

        authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        refsNode = _mapper.createArrayNode();
        refsNode.add("ref" + String.format(schema.getIdFormat("reference"), 1));
        refsNode.add("ref" + String.format(schema.getIdFormat("reference"), 2));

        on.put("refs", refsNode);
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Michal");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationUser", "Eve");
        on.put("creationTime", sdf.format(new Date(System.currentTimeMillis())));
        _insights.add(_mapper.writeValueAsString(on));
    }

    private final String cReference = "reference";
    private final String cIndexType = "pge";

    private void BulkLoadReferences() {
        BulkRequestBuilder bulk = client.prepareBulk();
        for(int j=1; j<=_references.size(); j++) {
            String referenceId = "ref" + String.format(schema.getIdFormat(cReference), j);
            String index = Stream.ofAll(schema.getPartitions(cReference)).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
            bulk.add(client.prepareIndex().setIndex(index).setType(cIndexType).setOpType(IndexRequest.OpType.INDEX).setId(referenceId).setSource(_references.get(j-1), XContentType.JSON)).get();
        }
        bulk.execute();
    }

    private void BulkLoadEntitiesAndEntityValues() {
        BulkRequestBuilder bulk = client.prepareBulk();

        String index = Stream.ofAll(schema.getPartitions(cEntity)).map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(getEntityId(1))).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

        List<String> contexts = Arrays.asList("context1", "global");
        for(int i=1; i<=_entities.size(); i++) {
            String myLogicalId = getEntityId(i);

            int finalI = i;
            contexts.forEach(ctx -> bulk.add(client.prepareIndex().setIndex(index).setType(cIndexType).setId(myLogicalId + "." + ctx)
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(myLogicalId)
                    .setSource(_entities.get(finalI -1), XContentType.JSON)).get());
        }

        for(int i=1; i<=_entitiesValues.size(); i++) {
            String logicalId = getEntityId(_evEntityId.get(i-1));
            bulk.add(client.prepareIndex().setIndex(index).setType(cIndexType).setId("ev" + i)
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                    .setSource(_entitiesValues.get(i-1), XContentType.JSON)).get();
        }
        bulk.execute();
    }

    public long load() throws IOException {
        long count = 0;
        int currentEntityLogicalId = 0;
        int evalueId = 0;
        _mapper = new ObjectMapper();

        Random random = new Random();

        List<String> myRefs = Arrays.asList("{\"authorization\":[\"source1.procedure1\",\"source2.procedure2\"],\"system\":\"system7\",\"creationUser\":\"Arla Navarrette\",\"creationTime\":\"2018-04-26 14:02:40.533\",\"authorizationCount\":1,\"lastUpdateUser\":\"Lucia Markel\",\"type\":\"reference\",\"title\":\"Title of - ref00000000\",\"value\":\"principle of selection: he rejects pleasures to secure other greater pleasures, or else he endures\",\"url\":\"http://8161718d-5e74-42d0-8146-9c28f2310a0e.la\",\"lastUpdateTime\":\"2018-04-26 14:02:40.533\"}",
                "{\"authorization\":[\"source1.procedure1\",\"source2.procedure2\"],\"system\":\"system7\",\"creationUser\":\"Test2\",\"creationTime\":\"2018-04-26 14:02:40.533\",\"authorizationCount\":1,\"lastUpdateUser\":\"Lucia Markel\",\"type\":\"reference\",\"title\":\"Title of - ref00000001\",\"value\":\"principle of selection: he rejects pleasures to secure other greater pleasures, or else he endures\",\"url\":\"http://8161718d-5e74-42d0-8146-9c28f2310a0e.la\",\"lastUpdateTime\":\"2018-04-26 14:02:40.533\"}");

        // List<String> contexts = Arrays.asList("context1", "context2", "context3", "global");
        List<String> contexts = Arrays.asList("context1", "context2");
        List<String> users = Arrays.asList("Tonette Kwon", "Georgiana Vanasse", "Tena Barriere", "Sharilyn Dennis", "Yee Edgell", "Berneice Luz",
                "Jasmin Mullally", "Suzette Saenger", "Jeri Miltenberger", "Lea Herren", "Brendon Richard", "Sonja Feeney", "Marcene Caffey",
                "Lelia Kott", "Arletta Kollman", "Hien Vrabel", "Marguerita Willingham", "Oleta Specht", "Calista Clutter", "Elliot Dames",
                "Kizzy Seekins", "Jodi Michaelis", "Curtis Yelvington", "Christina Bandy", "Ivory Capoccia", "Shakia Blakes", "Sindy Uselton",
                "Pam Delong", "Beatrice Hix", "Kimbra Fiorenza", "Rodolfo Manthey", "Rosella Dann", "Azalee Jess", "Gale Dedios", "Alaine Le",
                "Hope Brady", "Irene Dodrill", "Adrian Mister", "Doria Stacks", "Charlsie Iser", "Jean Lejeune", "Arla Navarrette", "Cara Commander",
                "Zada Puthoff", "Micaela Pearlman", "Domenica Charters", "Brady Scheffler", "Signe Ketner", "Myrtle Macarthur", "Jamar Kissner",
                "Ethelene Lacoste", "Lance Odonnell", "Lisandra Garceau", "Millie Ocon", "Hershel Aldana", "Kelley Ketner", "Janette Limones",
                "Arnetta Arriaga", "Luis Hugo", "Racquel Vannorman", "Rosalind Foland", "Melaine Boerner", "Ivy Monty", "Huey Walke", "Tasha Fairless",
                "Orval Everton", "Cathern Legge", "Vida Seely", "Lee Knoll", "Lucia Markel", "Brigette Wolfe", "Gita Ekstrom", "Porter Hillin",
                "Carolyne Conway", "Fred Nye", "Carlo Crandell", "Syreeta Hahne", "Katy Thibault", "Corazon Hagstrom", "Zina Teston", "Doyle Cavalier",
                "Freddie Wardlaw", "Sherley Windsor", "Iraida Quade", "Doria Andrews", "Luz Flavin", "Su Loper", "Mitchell Luster", "Arnulfo Bleakley",
                "Sharolyn Pooler", "Benita Vantassell", "Mui Huls", "Susann Stoughton", "Prince Dearth", "Saul Tomasini", "Luise Kinnaman",
                "Willette Madison", "Fernando Bransford", "Necole Haan", "Irmgard Gerardo");

        List<String> descriptions = Arrays.asList("District Sales Manager", "E-Commerce Director", "Export Manager", "Regional Sales Manager",
                "Sales Account Manager", "Sales Director", "Territory Sales Manager", "Contract Administrator", "Contracting Manager",
                "Director of Strategic Sourcing", "Procurement Manager", "Purchasing Director", "Purchasing Manager", "Sourcing Manager",
                "CEO", "Chief Executive Officer", "Chief Operating Officer", "Commissioner of Internal Revenue", "COO", "County Commissioner",
                "Government Service Executive", "Governor", "Mayor", "Clerk of Court", "Director of Entertainment", "Environmental Control Administrator",
                "Highway Patrol Commander", "Safety Coordinator", "Social Science Manager", "Utilities Manager", "Construction Coordinator",
                "Construction Superintendent", "General Contractor", "Masonry Contractor Administrator", "C++ Professor",
                "Computer Information Systems Professor", "Computer Programming Professor", "Information Systems Professor",
                "Information Technology Professor", "IT Professor", "Java Programming Professor", "Electrical Design Engineer", "Electrical Engineer",
                "Electrical Systems Engineer", "Illuminating Engineer", "Power Distribution Engineer", "Air Battle Manager", "Airdrop Systems Technician",
                "Astronaut, Mission Specialist", "Fixed-Wing Transport Aircraft Specialist", "Helicopter Officer",
                "Naval Flight Officer, Airborne Reconnaissance Officer", "Naval Flight Officer, Bombardier/Navigator",
                "Naval Flight Officer, Electronic Warfare Officer", "Naval Flight Officer, Qualified Supporting Arms Coordinator (Airborne)",
                "Naval Flight Officer, Radar Intercept Officer", "Naval Flight Officer, Weapons Systems Officer",
                "Special Project Airborne Electronics Evaluator", "Advanced Seal Delivery System", "Combatant Diver Officer",
                "Combatant Diver Qualified (Officer)", "Commanding Officer, Special Warfare Team", "Control And Recovery, Combat Rescue",
                "Control And Recovery, Special Tactics", "Executive Officer, Special Warfare Team", "Parachute/Combatant Diver Officer",
                "Parachutist/Combatant Diver Qualified (Officer)", "Sea-Air-Land Officer", "Seal Delivery Vehicle Officer", "Special Forces Officer",
                "Special Forces Warrant Officer", "Special Weapons Unit Officer");

        List<String> nicknames = Arrays.asList("Babe", "Bitsy", "Dumdum", "Shy", "Scruffy", "Spider", "Sugar", "Boogie",
                "Twinkle Toes", "Ginger", "Mamba", "Tricky", "Stone", "Tiny", "Gus", "Cuddles", "Brow", "Happy",
                "Pugs", "Smitty", "Smasher", "Dusty", "Piggy", "Comet", "Chappie", "Gentle", "Punch", "Machine", "Bing",
                "Mugs", "Rouge", "Sandy", "Bambam", "Diamond", "Butterfly", "Mac", "Scoop", "Wiz", "Old Buck", "Duke",
                "Artsy", "Biggie", "Nimble", "Dawg", "Ox", "Flash", "Dizzy", "Captain", "Mugsy", "Basher", "Growl", "Yank",
                "Aqua", "Dice", "Dimple", "Big Boy", "Hurricane", "Birds", "Beauty", "Twinkle", "Jumper", "Snake", "Sailor",
                "Spud", "Berry", "Blush", "Skin", "Undertaker", "Snowflake", "Gem", "Jazzy", "Tiger", "Peanut", "Mitzi",
                "Sparrow", "Honesty", "Stout", "Jolly", "Jelly", "Maniac", "Magic", "Dynamite", "Handsome", "Grouch", "Doc",
                "Ducky", "Bash", "Toon", "Major", "Cutie", "Dino", "Mad Dog", "Rip", "Rusty", "Queen Bee", "Cyclops", "Pipi",
                "Sizzle", "Goose", "Pitch", "Jumbo", "Bones", "Tigress", "Flip", "Bigshot", "Little", "Vulture", "Lucky",
                "Worm", "Buster", "Guns", "Camille", "Mistletoe", "Gator", "Chip", "Prince", "Wonder", "Fury", "Creep", "Dog",
                "Jacket", "Silence", "Dodo", "Flutters", "Groovy", "Ziggy", "Jackal", "Boots", "Landslide", "Assassin", "Dagger",
                "Jewel", "Admiral", "Terminator", "Bulldog");

        List<String> domains = Arrays.asList("com", "co.uk", "gov", "org", "net", "me", "ac", "ca", "biz", "cx", "dk", "es", "eu",
                "gd", "gy", "in", "it", "la", "nz", "ph", "se", "yt");

        List<String> contents = Arrays.asList(
                "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was",
                "born and I will give you a complete account of the system, and expound the actual teachings of the",
                "great explorer of the truth, the master-builder of human happiness. No one rejects, dislikes, or",
                "avoids pleasure itself, because it is pleasure, but because those who do not know how to pursue",
                "pleasure rationally encounter consequences that are extremely painful. Nor again is there anyone",
                "who loves or pursues or desires to obtain pain of itself, because it is pain, but because",
                "occasionally circumstances occur in which toil and pain can procure him some great pleasure. To",
                "take a trivial example, which of us ever undertakes laborious physical exercise, except to obtain",
                "some advantage from it? But who has any right to find fault with a man who chooses to enjoy a",
                "pleasure that has no annoying consequences, or one who avoids a pain that produces no resultant",
                "pleasure? On the other hand, we denounce with righteous indignation and dislike men who are so",
                "beguiled and demoralized by the charms of pleasure of the moment, so blinded by desire, that they",
                "cannot foresee the pain and trouble that are bound to ensue; and equal blame belongs to those who",
                "fail in their duty through weakness of will, which is the same as saying through shrinking from",
                "toil and pain. These cases are perfectly simple and easy to distinguish. In a free hour, when our",
                "power of choice is untrammelled and when nothing prevents our being able to do what we like best,",
                "every pleasure is to be welcomed and every pain avoided. But in certain circumstances and owing to",
                "the claims of duty or the obligations of business it will frequently occur that pleasures have to",
                "be repudiated and annoyances accepted. The wise man therefore always holds in these matters to this",
                "principle of selection: he rejects pleasures to secure other greater pleasures, or else he endures",
                "pains to avoid worse pains. But I must explain to you how all this mistaken idea of denouncing",
                "pleasure and praising pain was born and I will give you a complete account of the system, and",
                "expound the actual teachings of the great explorer of the truth, the master-builder of human",
                "happiness. No one rejects, dislikes, or avoids pleasure itself, because it is pleasure, but because",
                "those who do not know how to pursue pleasure rationally encounter consequences that are extremely",
                "painful. Nor again is there anyone who loves or pursues or desires to obtain pain of itself,",
                "because it is pain, but because occasionally circumstances occur in which toil and pain can procure",
                "him some great pleasure. To take a trivial example, which of us ever undertakes laborious physical",
                "exercise, except to obtain some advantage from it? But who has any right to find fault with a man",
                "who chooses to enjoy a pleasure that has no annoying consequences, or one who avoids a pain that",
                "produces no resultant pleasure? On the other hand, we denounce with righteous indignation and",
                "dislike men who are so beguiled and demoralized by the charms of pleasure of the moment, so blinded",
                "by desire, that they cannot foresee the pain and trouble that are bound to ensue; and equal blame",
                "belongs to those who fail in their duty through weakness of will, which is the same as saying",
                "through shrinking from toil and pain. These cases are perfectly simple and easy to distinguish. In",
                "a free hour, when our power of choice is untrammelled and when nothing prevents our being able to",
                "do what we like best, every pleasure is to be welcomed and every pain avoided. But in certain",
                "circumstances and owing to the claims of duty or the obligations of business it will frequently",
                "occur that pleasures have to be repudiated and annoyances accepted. The wise man therefore always",
                "holds in these matters to this principle of selection: he rejects pleasures to secure other greater",
                "pleasures, or else he endures pains to avoid worse pains.But I must explain to you how all this",
                "mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete",
                "account of the system, and expound the actual teachings of the great explorer of the truth, the",
                "master-builder of human happiness. No one rejects, dislikes, or avoids pleasure itself, because it",
                "is pleasure, but because those who do not know how to pursue pleasure rationally encounter",
                "consequences that are extremely painful. Nor again is there anyone who loves or pursues or desires",
                "to obtain pain of itself, because it is pain, but because occasionally circumstances occur in which",
                "toil and pain can procure him some great pleasure. To take a trivial example, which of us ever",
                "undertakes laborious physical exercise, except to obtain some advantage from it? But who has any",
                "right to find fault with a man who chooses to enjoy a pleasure that has no annoying consequences,",
                "or one who avoids a pain that produces no resultant pleasure? On the other hand, we denounce with",
                "righteous indignation and dislike men who are so beguiled and demoralized by the charms of pleasure",
                "of the moment, so blinded by desire, that they cannot foresee the pain and trouble that are bound",
                "to ensue; and equal blame belongs to those who fail in their duty through weakness of will, which",
                "is the same as saying through shrinking from toil and pain. These cases are perfectly simple and",
                "easy to distinguish. In a free hour, when our power of choice is untrammelled and when nothing",
                "prevents our being able to do what we like best, every pleasure is to be welcomed and every pain",
                "avoided. But in certain circumstances and owing to the claims of duty or the obligations of",
                "business it will frequently occur that pleasures have to be repudiated and annoyances accepted. The",
                "wise man therefore always holds in these matters to this principle of selection:");

        PrepareReferenceValues();
        BulkLoadReferences();

        PrepareEntityValues();
        PrepareEntityValuesValues();

        BulkLoadEntitiesAndEntityValues();

        BulkRequestBuilder bulk = client.prepareBulk();
        // Inserting single relation
        /*
        String relationIdString = "r" + String.format(schema.getIdFormat("relation"), 0);
        String index = Stream.ofAll(schema.getPartitions("relation")).map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(relationIdString)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
        String category = "own";

        bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(relationIdString)
                .setOpType(IndexRequest.OpType.INDEX)
                .setSource(new MapBuilder<String, Object>()
                        .put("type", "relation")
                        .put("entityAId", "e" + String.format(schema.getIdFormat("entity"), 1) + ".context1")
                        .put("entityACategory", "person")
                        .put("entityALogicalId", "e" + String.format(schema.getIdFormat("entity"), 1))
                        .put("entityBId", "e" + String.format(schema.getIdFormat("entity"), 2) + ".context2")
                        .put("entityBCategory", "person")
                        .put("entityBLogicalId", "e" + String.format(schema.getIdFormat("entity"), 1))
                        .put("context", "context1")
                        .put("category", category)
                        .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                        .put("authorizationCount", 1)
                        .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                .toJavaList())
                        .put("lastUpdateUser", "User")
                        .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                        .put("creationUser", "User1")
                        .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));
        bulk.execute();
        */

        //return count;
        return 0;
    }
}

