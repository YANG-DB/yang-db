package com.kayhut.fuse.assembly.knowlegde;

import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.schema.InitialGraphDataLoader;
import com.kayhut.fuse.executor.ontology.schema.RawElasticSchema;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
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
import org.elasticsearch.client.Client;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class InitialKnowledgeDataLoader implements InitialGraphDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(InitialKnowledgeDataLoader.class);

    private TransportClient client;
    private SimpleDateFormat sdf;
    private Config conf;
    private RawElasticSchema schema;

    @Inject
    public InitialKnowledgeDataLoader(Config conf, RawElasticSchema schema) throws UnknownHostException {
        this.conf = conf;
        this.schema = schema;
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
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public long init() throws IOException {
        String workingDir = System.getProperty("user.dir");
        File templates = Paths.get(workingDir, "indexTemplates").toFile();
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

    @Override
    public long drop() throws IOException {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    public long load() throws IOException {
        long count = 0;
        int currentEntityLogicalId = 0;
        int evalueId = 0;

        Random random = new Random();

        List<String> contexts = Arrays.asList("context1", "context2", "context3", "global");
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

        BulkRequestBuilder bulk = client.prepareBulk();
        for (int refId = 0; refId < 400; refId++) {
            String referenceId = "ref" + String.format(schema.getIdFormat("reference"), refId);
            String index = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(referenceId)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(new MapBuilder<String, Object>()
                            .put("type", "reference")
                            .put("title", "Title of - " + referenceId)
                            .put("url", "http://" + UUID.randomUUID().toString() + "." + domains.get(random.nextInt(domains.size())))
                            .put("value", contents.get(random.nextInt(contents.size())))
                            .put("system", "system" + random.nextInt(10))
                            .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                            .put("authorizationCount", 1)
                            .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                            .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                            .put("creationUser", users.get(random.nextInt(users.size())))
                            .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));
        }
        count += bulk.execute().actionGet().getItems().length;

        bulk = client.prepareBulk();
        while (currentEntityLogicalId < 100) {
            for (String context : contexts) {
                String logicalId = "e" + String.format(schema.getIdFormat("entity"), currentEntityLogicalId);
                String index = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                String category = "person";
                String description = descriptions.get(random.nextInt(descriptions.size()));
                List<String> personNicknames = Stream.ofAll(Arrays.asList(nicknames.get(random.nextInt(nicknames.size())),
                        nicknames.get(random.nextInt(nicknames.size())),
                        nicknames.get(random.nextInt(nicknames.size())),
                        nicknames.get(random.nextInt(nicknames.size())),
                        nicknames.get(random.nextInt(nicknames.size())),
                        nicknames.get(random.nextInt(nicknames.size()))))
                        .distinct().take(random.nextInt(2) + 1).toJavaList();

                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(logicalId + "." + context)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "entity")
                                .put("logicalId", logicalId)
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                if (context.equals("global")) {
                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "title")
                                    .put("bdt", "title")
                                    .put("stringValue", users.get(currentEntityLogicalId))
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "description")
                                    .put("bdt", "description")
                                    .put("stringValue", description)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    for (String personNickname : personNicknames) {
                        bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                                .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                                .setSource(new MapBuilder<String, Object>()
                                        .put("type", "e.value")
                                        .put("logicalId", logicalId)
                                        .put("entityId", logicalId + "." + context)
                                        .put("context", context)
                                        .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                        .put("authorizationCount", 1)
                                        .put("fieldId", "nicknames")
                                        .put("bdt", "nicknames")
                                        .put("stringValue", personNickname)
                                        .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                                .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                                .toJavaList())
                                        .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                        .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                        .put("creationUser", users.get(random.nextInt(users.size())))
                                        .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                        .get()));
                    }
                } else if (context.equals("context3")) {

                } else {
                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "name")
                                    .put("bdt", "name")
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("stringValue", users.get(currentEntityLogicalId))
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    int age = random.nextInt(120);
                    int anotherAge = age + (random.nextInt(8) - 4);

                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", age)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", anotherAge)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", anotherAge)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("deleteUser", users.get(random.nextInt(users.size())))
                                    .put("deleteTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                }
            }

            currentEntityLogicalId++;
        }
        count += bulk.execute().actionGet().getItems().length;

        bulk = client.prepareBulk();
        List<String> colors = Arrays.asList("red", "blue", "green", "white", "black", "brown", "orange", "purple", "pink", "yellow");
        for (int i = 0; i < 20; i++, currentEntityLogicalId++) {
            for (String context : contexts) {
                String logicalId = "e" + String.format(schema.getIdFormat("entity"), currentEntityLogicalId);
                String index = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                String category = ((i / 5) % 2) == 0 ? "car" : "boat";
                String color = colors.get(random.nextInt(colors.size()));
                String title = color + " " + category;
                String description = title;

                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(logicalId + "." + context)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "entity")
                                .put("logicalId", logicalId)
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                if (context.equals("global")) {
                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "title")
                                    .put("bdt", "title")
                                    .put("stringValue", title)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "description")
                                    .put("bdt", "description")
                                    .put("stringValue", description)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                } else {
                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "color")
                                    .put("bdt", "color")
                                    .put("stringValue", color)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.value")
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "licenseNumber")
                                    .put("bdt", "licenseNumber")
                                    .put("stringValue", UUID.randomUUID().toString().substring(0, 8))
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                }
            }
        }
        count += bulk.execute().actionGet().getItems().length;

        bulk = client.prepareBulk();
        int relationId = 0;
        int rvalueId = 0;
        for (int i = 0; i < 20; i++) {
            for (String context : Stream.ofAll(contexts).filter(context -> !context.equals("global"))) {
                String relationIdString = "r" + String.format(schema.getIdFormat("relation"), relationId++);
                String index = Stream.ofAll(schema.getPartitions("relation")).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(relationIdString)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                String category = "own";

                String personLogicalId = "e" + String.format(schema.getIdFormat("entity"), i);
                String personEntityId = personLogicalId + "." + context;
                String personIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(personLogicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

                String propertyLogicalId = "e" + String.format(schema.getIdFormat("entity"), 100 + i);
                String propertyEntityId = propertyLogicalId + "." + context;
                String propertyCategory = ((i / 5) % 2) == 0 ? "car" : "boat";
                String propertyIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(propertyLogicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

                String relationLastUpdateUser = users.get(random.nextInt(users.size()));
                String relationCreationUser = users.get(random.nextInt(users.size()));
                String relationLastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
                String relationCreateTime = sdf.format(new Date(System.currentTimeMillis()));

                bulk.add(client.prepareIndex().setIndex(personIndex).setType("pge").setId(relationIdString + ".out")
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(personLogicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "e.relation")
                                .put("entityAId", personEntityId)
                                .put("entityACategory", "person")
                                .put("entityBId", propertyEntityId)
                                .put("entityBCategory", propertyCategory)
                                .put("relationId", relationIdString)
                                .put("direction", "out")
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationUser", relationCreationUser)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(propertyIndex).setType("pge").setId(relationIdString + ".in")
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(propertyLogicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "e.relation")
                                .put("entityBId", personEntityId)
                                .put("entityBCategory", "person")
                                .put("entityAId", propertyEntityId)
                                .put("entityACategory", propertyCategory)
                                .put("relationId", relationIdString)
                                .put("direction", "in")
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationUser", relationCreationUser)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(relationIdString)
                        .setOpType(IndexRequest.OpType.INDEX)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "relation")
                                .put("entityAId", personEntityId)
                                .put("entityACategory", "person")
                                .put("entityALogicalId", personLogicalId)
                                .put("entityBId", propertyEntityId)
                                .put("entityBCategory", propertyCategory)
                                .put("entityBLogicalId", propertyLogicalId)
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationUser", relationCreationUser)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("rv" + rvalueId++)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "r.value")
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("fieldId", "since")
                                .put("bdt", "date")
                                .put("dateValue", sdf.format(new Date(System.currentTimeMillis())))
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("rv" + rvalueId++)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "r.value")
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("fieldId", "paid")
                                .put("bdt", "payment")
                                .put("intValue", random.nextInt(1000))
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId("rv" + rvalueId++)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "r.value")
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("fieldId", "paid")
                                .put("bdt", "payment")
                                .put("intValue", random.nextInt(1000))
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));
            }
        }
        count += bulk.execute().actionGet().getItems().length;

        bulk = client.prepareBulk();
        int iId = 0;
        for (int entityId = 0; entityId < 100; entityId++) {
            List<String> logicalIds = Stream.ofAll(Arrays.asList(
                    entityId, (entityId + 1) % 100, (entityId + 2) % 100, (entityId + 3) % 100))
                    .map(id -> "e" + String.format(schema.getIdFormat("entity"), id))
                    .toJavaList();

            for (String context : Stream.ofAll(contexts).filter(context -> !context.equals("global") && !context.equals("context3"))) {
                String insightId = "i" + String.format(schema.getIdFormat("insight"), iId++);
                String index = Stream.ofAll(schema.getPartitions("insight")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                        .filter(partition -> partition.isWithin(insightId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);


                bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(insightId)
                        .setOpType(IndexRequest.OpType.INDEX)
                        .setSource(new MapBuilder<String, Object>()
                                .put("type", "insight")
                                .put("content", contents.get(random.nextInt(contents.size())))
                                .put("context", context)
                                .put("entityIds", Stream.ofAll(logicalIds).map(logicalId -> logicalId + "." + context).toJavaList())
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(schema.getIdFormat("reference"), refId))
                                        .toJavaList())
                                .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                for (String logicalId : logicalIds) {
                    String logicalEntityIndex =
                            Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                                    .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

                    bulk.add(client.prepareIndex().setIndex(logicalEntityIndex).setType("pge").setId(logicalId + "." + insightId)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("type", "e.insight")
                                    .put("entityId", logicalId + "." + context)
                                    .put("insightId", insightId).get()));
                }
            }

        }
        count += bulk.execute().actionGet().getItems().length;
        return count;

    }
}
