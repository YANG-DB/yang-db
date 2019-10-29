package com.yangdb.fuse.assembly.knowledge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReference;
import com.yangdb.fuse.assembly.knowledge.domain.Reference;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
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
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Moti
 */
public class RankingKnowledgeDataInfraManager {
    private static final Logger logger = LoggerFactory.getLogger(RankingKnowledgeDataInfraManager.class);

    private Client client;
    private SimpleDateFormat sdf;
    private Config conf;
    private RawSchema schema;

    public RankingKnowledgeDataInfraManager(String confPath, Client client) throws UnknownHostException {
        this.client = client;
        try {
            File configFile = new File(confPath);
            this.conf = ConfigFactory.parseFileAnySyntax(configFile, ConfigParseOptions.defaults().setAllowMissing(false));
            this.schema = ((Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_raw_schema"))).newInstance();

            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        } catch (Exception exc) {

        }
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

        Iterable<String> allIndices = getIndices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
    }

    public long drop()  {
        Iterable<String> indices = Stream.ofAll(getIndices()).append(".idgenerator");
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

    private Iterable<String> getIndices() {
        return schema.indices(partition -> !(partition instanceof IndexPartitions.Partition.Default<?>));
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
                Arrays.asList("Moti", "Moti cohen"),
                Arrays.asList("Moti"),
                Arrays.asList("Moti cohen", "roman"),
                Arrays.asList("Motic"),
                Arrays.asList("roman"),
                Arrays.asList("tonette kwon"),
                Arrays.asList("veorgiana Vanasse"),
                Arrays.asList("veorgiana Vanasse SUZETTE"),
                Arrays.asList("SUZETTE veorgiana Vanasse"),
                Arrays.asList("aaa Bbb eee ccc"),
                Arrays.asList("aaa Bbb cccd"),
                Arrays.asList("laaa Bbb cccd"),
                Arrays.asList("aaa ccc"),
                Arrays.asList("aaa ccc Bbb"),
                Arrays.asList("laaa cccd"),
                Arrays.asList("abcdefghijklmnop"),
                Arrays.asList("abcdefghijklmnopccc"),
                Arrays.asList("abcdef???ghijklm"),
                Arrays.asList("aaa? ccc? Bbb?"),
                Arrays.asList("?tonette? ?kwon"),
                Arrays.asList("  OMG  ")

        );
        List<List<String>> titles = Arrays.asList(
                Arrays.asList("Moti cohen"),
                Arrays.asList("Moti"),
                Arrays.asList("roman"),
                Arrays.asList("Motic"),
                Arrays.asList("roman"),
                Arrays.asList("tonette kwon"),
                Arrays.asList("veorgiana Vanasse"),
                Arrays.asList("veorgiana Vanasse SUZETTE"),
                Arrays.asList("SUZETTE veorgiana Vanasse"),
                Arrays.asList("aaa Bbb eee ccc"),
                Arrays.asList("aaa Bbb cccd"),
                Arrays.asList("laaa Bbb cccd"),
                Arrays.asList("aaa ccc"),
                Arrays.asList("aaa ccc Bbb"),
                Arrays.asList("laaa cccd"),
                Arrays.asList("babababababababaab"),
                Arrays.asList("babababababababaabccc"),
                Arrays.asList("abcdef???ghijklm"),
                Arrays.asList("aaa? ccc? Bbb?"),
                Arrays.asList("?tonette? ?kwon"),
                Arrays.asList("  OMG  ")


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

    private int BulkLoadEntitiesAndEntityValues() throws ExecutionException, InterruptedException {
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
        final BulkResponse responses = bulk.execute().get();
        return responses.getItems().length;
    }

    public long load() throws IOException, ExecutionException, InterruptedException {
        _mapper = new ObjectMapper();

        PrepareReferenceValues();
        BulkLoadReferences();

        PrepareEntityValues();
        PrepareEntityValuesValues();

        final int count = BulkLoadEntitiesAndEntityValues();

        //return count;
        return count;
    }
}

