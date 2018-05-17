package com.kayhut.fuse.assembly.knowlegde;

import com.cedarsoftware.util.io.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import io.swagger.util.Json;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rani y. on 5/6/2018
 */
public class KnowledgeDataInfraManager  {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDataInfraManager.class);
    private TransportClient client;
    private SimpleDateFormat sdf;
    private Config conf;
    private RawSchema schema;
    private ObjectMapper _mapper;
    private List<String> _entities, _inRelations, _outRelations, _relationValues, _entitiesValues, _insights, _insightsEntities, _relations, _references;
    private final String cEntityValue = "e.value";
    private final String cInsight = "insight";
    private final String cReference = "reference";
    private final String cIndexType = "pge";
    private final String cRelation = "relation";
    private final String cEntity = "entity";

    public KnowledgeDataInfraManager(String confPath) throws UnknownHostException {
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
        String workingDir = /*"c:\\Users\\rani\\Documents\\Fuse\\fuse-assembly";*/ System.getProperty("user.dir");
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

    public long drop() throws IOException {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    private void PrepareReferenceValues() throws JsonProcessingException {
        _references = new ArrayList<String>();

        //List<String> auth = Arrays.asList("source1.procedure1", "source1.procedure1");
        ObjectNode on  = _mapper.createObjectNode();
        on.put("system", "system7");
        on.put("type", "reference");
        on.put("creationUser", "Arla Nava");
        on.put("lastUpdateUser", "Lucia Mark");
        on.put("url", "http://1.1.1.1:6200/clown");
        on.put("title", "Sample Title 1");
        on.put("creationTime","2018-04-26 14:02:40.533");
        on.put("lastUpdateTime","2018-04-29 13:02:40.133");

        ArrayNode auth = _mapper.createArrayNode();
        auth.add("source1.procedure1");
        auth.add("source1.procedure1");

        on.put("authorization", auth);

        _references.add(_mapper.writeValueAsString(on));

        on  = _mapper.createObjectNode();
        on.put("system", "system7");
        on.put("type", "reference");
        on.put("creationUser", "Haim Moshe");
        on.put("lastUpdateUser", "Mark Rucinovitch");
        on.put("url", "http://1.1.1.1:6200/circus");
        on.put("title", "Sample Title 2");
        on.put("creationTime","2018-04-24 14:02:40.533");
        on.put("lastUpdateTime","2018-04-27 13:02:40.133");

        on.put("authorization", auth);

        _references.add(_mapper.writeValueAsString(on));
    }

    private void PrepareEntityValues() throws JsonProcessingException {
        _entities = new ArrayList<String>();
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cEntity);
        on.put("logicalId", "e" + String.format(schema.getIdFormat(cEntity), 1));
        on.put("context", "context1");
        on.put("category", "person");
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Mark");
        on.put("creationUser", "Hassan");
        on.put("lastUpdateTime", "2018-03-24 14:02:40.533");
        on.put("creationTime", "2018-03-26 12:02:40.133");

        ArrayNode refsNode = _mapper.createArrayNode();
        refsNode.add("ref" + String.format(schema.getIdFormat("reference"), 1));
        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);
        on.put("refs", refsNode);

        _entities.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", cEntity);
        on.put("logicalId", "e" + String.format(schema.getIdFormat(cEntity), 2));
        on.put("context", "context1");
        on.put("category", "person");
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Mark");
        on.put("creationUser", "Muhamad");
        on.put("lastUpdateTime", "2018-03-24 14:02:40.533");
        on.put("creationTime", "2018-03-27 11:12:40.133");

        refsNode = _mapper.createArrayNode();
        refsNode.add("ref" + String.format(schema.getIdFormat(cReference), 2));
        authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);
        on.put("refs", refsNode);

        _entities.add(_mapper.writeValueAsString(on));
    }

    private void PrepareRelationsAndValues() throws JsonProcessingException {
        _relations = new ArrayList<String>();
        _inRelations = new ArrayList<String>();
        _outRelations = new ArrayList<>();
        _relationValues = new ArrayList<>();

        String p1Identity = "e" + String.format(schema.getIdFormat(cEntity), 1) + ".context1";
        String p2Identity = "e" + String.format(schema.getIdFormat(cEntity), 2) + ".context1";

        // Relation 1
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cRelation);
        on.put("entityAId", p1Identity);
        on.put("entityACategory", "person");
        on.put("entityALogicalId", "e" + String.format(schema.getIdFormat("entity"), 1));
        on.put("entityBId", p2Identity);
        on.put("entityBCategory", "person");
        on.put("entityBLogicalId", "e" + String.format(schema.getIdFormat("entity"), 2));
        on.put("context", "context1");
        on.put("category", "friendOf");
        on.put("authorizationCount", 1);
        on.put("refs", "[]");
        on.put("lastUpdateUser", "Neta");
        on.put("lastUpdateTime", "2018-03-21 14:02:40.533");
        on.put("creationUser", "Shani");
        on.put("creationTime", "2018-02-18 14:02:40.533");

        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);

        _relations.add(_mapper.writeValueAsString(on));

        // In & Out parallel relations
        on = _mapper.createObjectNode();
        on.put("type", "e.relation");
        on.put("entityAId", p1Identity);
        on.put("entityACategory", "person");
        on.put("entityBId", p2Identity);
        on.put("entityBCategory", "person");
        on.put("relationId", "r" + String.format(schema.getIdFormat(cRelation), 1));
        on.put("direction", "out");
        on.put("context", "context1");
        on.put("category", "friendOf");
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Neta");
        on.put("lastUpdateTime", "2018-03-21 14:02:40.533");
        on.put("creationUser", "Shani");
        on.put("creationTime", "2018-02-18 14:02:40.533");

        _outRelations.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", "e.relation");
        on.put("entityBId", p1Identity);
        on.put("entityBCategory", "person");
        on.put("entityAId", p2Identity);
        on.put("entityACategory", "person");
        on.put("relationId", "r" + String.format(schema.getIdFormat(cRelation), 1));
        on.put("direction", "in");
        on.put("context", "context1");
        on.put("category", "friendOf");
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Neta");
        on.put("lastUpdateTime", "2018-03-21 14:02:40.533");
        on.put("creationUser", "Shani");
        on.put("creationTime", "2018-02-18 14:02:40.533");

        _inRelations.add(_mapper.writeValueAsString(on));

        // Relation 2
        on = _mapper.createObjectNode();
        on.put("type", cRelation);
        on.put("entityAId", "e" + String.format(schema.getIdFormat("entity"), 1) + ".context1");
        on.put("entityACategory", "person");
        on.put("entityALogicalId", "e" + String.format(schema.getIdFormat("entity"), 1));
        on.put("entityBId", "e" + String.format(schema.getIdFormat("entity"), 2) + ".context1");
        on.put("entityBCategory", "person");
        on.put("entityBLogicalId", "e" + String.format(schema.getIdFormat("entity"), 2));
        on.put("context", "context1");
        on.put("category", "siblingOf");
        on.put("authorizationCount", 1);
        on.put("refs", "[]");
        on.put("lastUpdateUser", "Neta");
        on.put("lastUpdateTime", "2018-02-21 11:02:40.533");
        on.put("creationUser", "Neta");
        on.put("creationTime", "2018-01-18 11:02:40.533");

        ArrayNode refsNode = _mapper.createArrayNode();
        refsNode.add("ref" + String.format(schema.getIdFormat("reference"), 1));

        on.put("refs", refsNode);

        on.put("authorization", authNode);

        _relations.add(_mapper.writeValueAsString(on));

        // In & Out parallel relations
        on = _mapper.createObjectNode();
        on.put("type", "e.relation");
        on.put("entityAId", p1Identity);
        on.put("entityACategory", "person");
        on.put("entityBId", p2Identity);
        on.put("entityBCategory", "person");
        on.put("relationId", "r" + String.format(schema.getIdFormat(cRelation), 2));
        on.put("direction", "out");
        on.put("context", "context1");
        on.put("category", "friendOf");
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Neta");
        on.put("lastUpdateTime", "2018-02-21 11:02:40.533");
        on.put("creationUser", "Neta");
        on.put("creationTime", "2018-01-18 11:02:40.533");

        _outRelations.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", "e.relation");
        on.put("entityBId", p1Identity);
        on.put("entityBCategory", "person");
        on.put("entityAId", p2Identity);
        on.put("entityACategory", "person");
        on.put("relationId", "r" + String.format(schema.getIdFormat(cRelation), 2));
        on.put("direction", "in");
        on.put("context", "context1");
        on.put("category", "friendOf");
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("lastUpdateUser", "Neta");
        on.put("lastUpdateTime", "2018-02-21 11:02:40.533");
        on.put("creationUser", "Neta");
        on.put("creationTime", "2018-01-18 11:02:40.533");

        _inRelations.add(_mapper.writeValueAsString(on));

        // Adding relation's values
        on = _mapper.createObjectNode();
        on.put("type", "r.value");
        on.put("relationId", "r" + String.format(schema.getIdFormat("relation"), 1));
        on.put("context", "context1");
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("fieldId", "sum");
        on.put("bdt", "sum");
        on.put("intValue", 2035);
        on.put("refs", "[]");
        on.put("lastUpdateUser", "Barzilay");
        on.put("lastUpdateTime", "2018-01-14 01:02:40.533");
        on.put("creationUser", "Barzilay");
        on.put("creationTime", "2018-01-12 02:02:40.533");

        _relationValues.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", "r.value");
        on.put("relationId", "r" + String.format(schema.getIdFormat("relation"), 2));
        on.put("context", "context1");
        on.put("authorization", authNode);
        on.put("authorizationCount", 1);
        on.put("fieldId", "sum");
        on.put("bdt", "sum");
        on.put("intValue", 1043);
        on.put("refs", "[]");
        on.put("lastUpdateUser", "Barzilay");
        on.put("lastUpdateTime", "2017-01-14 01:02:40.533");
        on.put("creationUser", "Barzilay");
        on.put("creationTime", "2017-01-12 02:02:40.533");

        _relationValues.add(_mapper.writeValueAsString(on));
    }

    private void PrepareEntityValuesValues() throws JsonProcessingException {
        _entitiesValues = new ArrayList<String>();

        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cEntityValue);
        on.put("logicalId", "e" + String.format(schema.getIdFormat("entity"), 1));
        on.put("entityId", "e" + String.format(schema.getIdFormat("entity"), 1) + ".context1");
        on.put("context", "context1");
        on.put("authorizationCount", 1);
        on.put("fieldId", "nicknames");
        on.put("bdt", "nicknames");
        on.put("stringValue", "Nick1");
        on.put("refs", "[\"\"]");
        on.put("lastUpdateUser", "Moses");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationUser", "Abraham");
        on.put("creationTime", sdf.format(new Date(System.currentTimeMillis())));

        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);

        _entitiesValues.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", cEntityValue);
        on.put("logicalId", "e" + String.format(schema.getIdFormat("entity"), 2));
        on.put("entityId", "e" + String.format(schema.getIdFormat("entity"), 2) + ".context1");
        on.put("context", "context1");
        on.put("authorizationCount", 1);
        on.put("fieldId", "nicknames");
        on.put("bdt", "nicknames");
        on.put("stringValue", "Nick2");
        on.put("refs", "[\"\"]");
        on.put("lastUpdateUser", "Moses");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationUser", "Abraham");
        on.put("creationTime", sdf.format(new Date(System.currentTimeMillis())));

        authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);
        _entitiesValues.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", cEntityValue);
        on.put("logicalId", "e" + String.format(schema.getIdFormat("entity"), 2));
        on.put("entityId", "e" + String.format(schema.getIdFormat("entity"), 2) + ".context1");
        on.put("context", "context1");
        on.put("authorizationCount", 1);
        on.put("fieldId", "age");
        on.put("bdt", "age");
        on.put("intValue", "14");
        on.put("refs", "[\"\"]");
        on.put("lastUpdateUser", "Moses");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationUser", "Abraham");
        on.put("creationTime", sdf.format(new Date(System.currentTimeMillis())));

        authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");

        on.put("authorization", authNode);
        _entitiesValues.add(_mapper.writeValueAsString(on));
    }

    private void PrepareInsights() throws JsonProcessingException {
        _insights = new ArrayList<String>();
        _insightsEntities = new ArrayList<String>();
        int id = 1;
        String insightId = "i" + String.format(schema.getIdFormat("insight"), id);

        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cInsight);
        on.put("content", "Very important, superb insight");
        on.put("context", "context1");

        ArrayNode entityIds = _mapper.createArrayNode();
        entityIds.add("e" + String.format(schema.getIdFormat("entity"), 1) + ".context1");

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
        on.put("type", "e.insight");
        on.put("insightId", insightId);
        on.put("entityId","e" + String.format(schema.getIdFormat("entity"), 1) + ".context1");
        _insightsEntities.add(_mapper.writeValueAsString(on));

        on = _mapper.createObjectNode();
        on.put("type", cInsight);
        on.put("content", "Quite poor, and mostly unuseful insight just tobe tested");
        on.put("context", "context1");

        entityIds = _mapper.createArrayNode();
        entityIds.add("e" + String.format(schema.getIdFormat("entity"), 1) + ".context1");
        entityIds.add("e" + String.format(schema.getIdFormat("entity"), 2) + ".context1");

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

        insightId = "i" + String.format(schema.getIdFormat("insight"), 2);
        on = _mapper.createObjectNode();
        on.put("type", "e.insight");
        on.put("insightId", insightId);
        on.put("entityId","e" + String.format(schema.getIdFormat("entity"), 1) + ".context1");
        _insightsEntities.add(_mapper.writeValueAsString(on));
        on = _mapper.createObjectNode();
        on.put("type", "e.insight");
        on.put("insightId", insightId);
        on.put("entityId","e" + String.format(schema.getIdFormat("entity"), 2) + ".context1");
        _insightsEntities.add(_mapper.writeValueAsString(on));
    }

    private void BulkLoadInsights() {
        BulkRequestBuilder bulk = client.prepareBulk();
        // Adding insight index
        for(int i=1; i<=2; i++) {
            String insightId = "i" + String.format(schema.getIdFormat(cInsight), i);
            String index = Stream.ofAll(schema.getPartitions(cInsight)).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(insightId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(index).setType(cIndexType).setId(insightId)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(_insights.get(i-1), XContentType.JSON));
        }

        List<Integer> insightsIds = Arrays.asList(1,2,2);
        List<Integer> entityLogicalIds = Arrays.asList(1,1,2);
        for(int i=0; i<_insightsEntities.size(); i++) {
            String logicalEntId = "e" + String.format(schema.getIdFormat("entity"), entityLogicalIds.get(i));
            String insightId = "i" + String.format(schema.getIdFormat(cInsight), insightsIds.get(i));
            String logicalEntityIndex =
                    Stream.ofAll(schema.getPartitions(cEntity)).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                            .filter(partition -> partition.isWithin(logicalEntId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(logicalEntityIndex).setType(cIndexType).setId(logicalEntId + "." + insightId)
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalEntId)
                    .setSource(_insightsEntities.get(i), XContentType.JSON));
        }
        bulk.execute();
    }

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
                .filter(partition -> partition.isWithin("e" + String.format(schema.getIdFormat(cEntity), 1))).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

        for(int i=1; i<=_entities.size(); i++) {
            String mylogicalId = "e" + String.format(schema.getIdFormat(cEntity), i);

            bulk.add(client.prepareIndex().setIndex(index).setType(cIndexType).setId(mylogicalId + "." + "context1")
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(mylogicalId)
                    .setSource(_entities.get(i-1), XContentType.JSON)).get();
        }

        List<Integer> evalues = Arrays.asList(1,2,2);

        for(int i=1; i<=_entitiesValues.size(); i++) {
            String logicalId = "e" + String.format(schema.getIdFormat("entity"), evalues.get(i-1));
            bulk.add(client.prepareIndex().setIndex(index).setType(cIndexType).setId("ev" + i)
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                    .setSource(_entitiesValues.get(i-1), XContentType.JSON)).get();
        }
        bulk.execute();
    }

    private void BulkLoadRelationsAndValues() {
        BulkRequestBuilder bulk = client.prepareBulk();

        String personLogicalId1 = "e" + String.format(schema.getIdFormat(cEntity), 1);
        String personLogicalId2 = "e" + String.format(schema.getIdFormat(cEntity), 2);

        String personIndex1 = Stream.ofAll(schema.getPartitions(cEntity)).map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(personLogicalId1)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
        String personIndex2 = Stream.ofAll(schema.getPartitions(cEntity)).map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(personLogicalId2)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

        for(int i=0;i<2; i++) {
            String relationIdString = "r" + String.format(schema.getIdFormat(cRelation), i);
            String relationIndex = Stream.ofAll(schema.getPartitions(cRelation)).map(partition -> (IndexPartitions.Partition.Range) partition)
                    .filter(partition -> partition.isWithin(relationIdString)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(personIndex1).setType(cIndexType).setId(relationIdString + ".out")
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(personLogicalId1)
                    .setSource(_outRelations.get(i), XContentType.JSON));
            bulk.add(client.prepareIndex().setIndex(personIndex2).setType(cIndexType).setId(relationIdString + ".in")
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(personLogicalId2)
                    .setSource(_inRelations.get(i), XContentType.JSON));
            bulk.add(client.prepareIndex().setIndex(relationIndex).setType(cIndexType).setId(relationIdString)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(_relations.get(i), XContentType.JSON));
            bulk.add(client.prepareIndex().setIndex(relationIndex).setType(cIndexType).setId("rv" + i)
                    .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                    .setSource(_relationValues.get(i), XContentType.JSON));
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

        PrepareRelationsAndValues();
        BulkLoadRelationsAndValues();

        PrepareInsights();
        BulkLoadInsights();

        return 0;
    }
}
