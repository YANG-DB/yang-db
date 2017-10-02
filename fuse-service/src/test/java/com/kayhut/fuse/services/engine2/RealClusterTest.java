package com.kayhut.fuse.services.engine2;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static java.util.Collections.singletonList;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class RealClusterTest {
    @Test
    public void test_fetchEntityById() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5, 6), 0),
                new EProp(3, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e015")),
                new EProp(4, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(5, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2")),
                new Rel(6, $ont.rType$("hasValue"), Rel.Direction.R, null, 7, 0),
                new ETyped(7, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 8, 0),
                new Quant1(8, QuantType.all, Arrays.asList(9, 10), 0),
                new EProp(9, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(10, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2"))
        )).build();

        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        long elapsed = System.currentTimeMillis() - start;
        int x = 5;
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 0, 0)
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test2() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasValue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 0, 0)
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }


    @Test
    public void test3() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "car")),
                new Rel(4, $ont.rType$("hasValue"), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 0, 0)
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test4() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "building")),
                new Rel(4, $ont.rType$("hasValue"), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 0, 0)
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test5() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "car")),
                new EProp(4, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(5, $ont.rType$("hasValue"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 0, 0)
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test6() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasValue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 4, 0),
                new Quant1(4, QuantType.all, Collections.singletonList(5), 0),
                new EProp(5, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test7() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasValue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 4, 0),
                new Quant1(4, QuantType.all, Collections.singletonList(5), 0),
                new EProp(5, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context2"))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test8() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasValue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Value"), $ont.$entity$("Value").getProperties(), 4, 0),
                new Quant1(4, QuantType.all, Arrays.asList(5, 6), 0),
                new EProp(5, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(6, $ont.pType$("propertyId"), Constraint.of(ConstraintOp.eq, "color"))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void loadData() throws UnknownHostException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "roman.es").build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        BulkRequestBuilder bulk = client.prepareBulk();

        List<String> colors = Arrays.asList("red", "blue", "green", "white", "black", "brown", "orange", "purple", "pink", "yellow");
        Random random = new Random();
        int valueId = 0;
        for(int i = 0 ; i < 20 ; i++) {
            for (String context : Arrays.asList("context1", "context2")) {
                String logicalId = "e" + String.format("%03d", i);
                String index = "entity" + ((i / 10) + 1);
                String category = ((i / 5) % 2) == 0 ? "car" : "boat";

                bulk.add(client.prepareIndex().setIndex(index).setType("Entity").setId(logicalId + "." + context)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("logicalId", logicalId)
                                .put("context", context)
                                .put("category", category)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("lastUpdateUser", UUID.randomUUID().toString())
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("Value").setId("v" + String.format("%03d", valueId++))
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("logicalId", logicalId)
                                .put("entityId", logicalId + "." + context)
                                .put("context", context)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("propertyId", "color")
                                .put("stringValue", colors.get(random.nextInt(colors.size())))
                                .put("lastUpdateUser", UUID.randomUUID().toString())
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("bdt", "color")
                                .get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("Value").setId("v" + String.format("%03d", valueId++))
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("logicalId", logicalId)
                                .put("entityId", logicalId + "." + context)
                                .put("context", context)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("propertyId", "licenseNumber")
                                .put("stringValue", UUID.randomUUID().toString())
                                .put("lastUpdateUser", UUID.randomUUID().toString())
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("bdt", "licenseNumber")
                                .get()));
            }
        }

        bulk.execute().actionGet();
    }
}
