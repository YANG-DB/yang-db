package com.yangdb.fuse.assembly.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.unipop.controller.utils.map.MapBuilder;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yangdb.fuse.asg.validation.AsgStepsValidatorStrategy.ENTITY;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeDataInfraManager.PGE;


public class KnowledgeAutomationFunctions {

    public static final String INDEX = "e0";


    static public String CreateKnowledgeEntity(ObjectMapper mapper, KnowledgeConfigManager manager, TransportClient client, String type,
                                               String logicalId, String context, String category, String lastUpdateUser,
                                               String creationUser, String lastUpdateTime, String creationTime,
                                               Integer authorizationCount, ArrayNode authorizationNode, ArrayNode refsNode)
            throws IOException {
        ArrayList<String> entities = new ArrayList<>();
        //create knowledge entity
        ObjectNode on = mapper.createObjectNode();
        on.put("type", type);
        on.put("logicalId", logicalId);
        on.put("context", context);
        on.put("category", category);
        on.put("lastUpdateUser", lastUpdateUser);
        on.put("creationUser", creationUser);
        on.put("lastUpdateTime", lastUpdateTime);
        on.put("creationTime", creationTime);
        on.put("authorizationCount", authorizationCount);
        on.put("authorization", authorizationNode); // Authorization = Clearance
        on.put("refs", refsNode);

        entities.add(mapper.writeValueAsString(on));
        BulkRequestBuilder bulk = client.prepareBulk();
        // Insert knowledge entity directly to elastic
        insertEntities(INDEX,context, manager.getSchema(), client, bulk, entities);
        return logicalId+"."+context;
    }

    static public int CreateKnowledgeReference(KnowledgeConfigManager manager, TransportClient client, int refNum) {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        BulkRequestBuilder bulk = client.prepareBulk();
        RawSchema schema = manager.getSchema();
        for (int refId = 0; refId < refNum; refId++) {
            String referenceId = "ref" + String.format(schema.getIdFormat("reference"), refId + 1);
            String index = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(referenceId)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(new MapBuilder<String, Object>()
                            .put("type", "reference")
                            .put("title", "Title of - " + referenceId)
                            .put("url", "https://stackoverflow.com/questions")
                            .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                            .put("authorizationCount", 1)
                            .put("lastUpdateUser", "2018-05-27 14:32:56.533")
                            .put("lastUpdateTime", "2018-05-27 14:32:56.533")
                            .put("creationUser", "2018-05-27 14:32:56.533")
                            .put("creationTime", "2018-05-27 14:32:56.533").get()));
        }
        int count = bulk.execute().actionGet().getItems().length;
        System.out.println("There are " + count + " references");
        return count;
    }

    static public int CreateKnowledgeFile(TransportClient client, String fileId, String logicalId, String entityId, int filedNum) {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        BulkRequestBuilder bulk = client.prepareBulk();
        for (int refId = 0; refId < filedNum; refId++) {
            bulk.add(client.prepareIndex().setIndex(INDEX).setType("pge").setId(fileId)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(new MapBuilder<String, Object>()
                            .put("type", "file")
                            .put("name", "name of - " + fileId)
                            .put("path", "http://" + UUID.randomUUID().toString() + "." + domains.get(random.nextInt(domains.size())))
                            .put("displayName", "display name of - " + fileId)
                            .put("mimeType", contexts.get(random.nextInt(contexts.size())))
                            .put("category", contexts.get(random.nextInt(contexts.size())))
                            .put("description", contents.get(random.nextInt(contents.size())))
                            .put("logicalId", logicalId)
                            .put("entityId", entityId)

                            //metadata
                            .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                            .put("authorizationCount", 1)
                            .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                            .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                            .put("creationUser", users.get(random.nextInt(users.size())))
                            .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));
        }
        int count = bulk.execute().actionGet().getItems().length;
        System.out.println("There are " + count + " files");
        return count;
    }


    static public QueryResultBase FetchCreatedEntity(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        // Create object of cursorRequest
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);
        // Waiting until it gets the response
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }


    static public int insertEntities(String index,String context, RawSchema schema, Client client, BulkRequestBuilder bulk, List<String> entities) {
        int count = 0;
        for (int i = 0; i < entities.size(); i++) {
            String mylogicalId = "e" + String.format(schema.getIdFormat(ENTITY), i);
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(index)
                    .setType(PGE)
                    .setId(mylogicalId + "." + context )
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setRouting(mylogicalId)
                    .setSource(entities.get(i), XContentType.JSON);
            count += bulk.add(request).get().getItems().length;
        }
        return count;
    }


    private static List<String> domains = Arrays.asList("com", "co.uk", "gov", "org", "net", "me", "ac");
    private static Random random = new Random();
    private static List<String> contexts = Arrays.asList("context1", "context2", "context3", "global");
    private static List<String> contents = Arrays.asList(
            "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was",
            "born and I will give you a complete account of the system, and expound the actual teachings of the");
    private static List<String> users = Arrays.asList("Tonette Kwon", "Georgiana Vanasse", "Tena Barriere", "Sharilyn Dennis");
    private static SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);

}
