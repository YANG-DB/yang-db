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
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static java.util.Collections.singletonList;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class RealClusterTest {
    @Test
    @Ignore
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
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 7, 0),
                new ETyped(7, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 8, 0),
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
    @Ignore
    public void test_fetchEntityById2() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5, 6, 7), 0),
                new EProp(3, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e004")),
                new EProp(4, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(5, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2")),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(11, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
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
    @Ignore
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
    @Ignore
    public void test2() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 0, 0)
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
    @Ignore
    public void test3() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "car")),
                new Rel(4, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 0, 0)
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
    @Ignore
    public void test4() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "building")),
                new Rel(4, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 0, 0)
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
    @Ignore
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
                new Rel(5, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 0, 0)
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
    @Ignore
    public void test6() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 4, 0),
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
    @Ignore
    public void test7() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 4, 0),
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
    @Ignore
    public void test8() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Rel(2, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 4, 0),
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
    @Ignore
    public void test9() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(5, $ont.rType$("hasRelation"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Relation"), $ont.$entity$("Relation").getProperties(), 0, 0)
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
    @Ignore
    public void test10() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(5, $ont.rType$("hasRelation"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Relation"), $ont.$entity$("Relation").getProperties(), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                new EProp(8, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "own")),
                new EProp(9, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
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
    @Ignore
    public void test11() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(5, $ont.rType$("hasRelation"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Relation"), $ont.$entity$("Relation").getProperties(), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                new EProp(8, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "has")),
                new EProp(9, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
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
    @Ignore
    public void test12() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(5, $ont.rType$("hasRelation"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Relation"), $ont.$entity$("Relation").getProperties(), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9, 10), 0),
                new EProp(8, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "own")),
                new EProp(9, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(10, $ont.rType$("hasRvalue"), Rel.Direction.R, null, 11, 0),
                new ETyped(11, "C", $ont.eType$("Rvalue"), $ont.$entity$("Rvalue").getProperties(), 0, 0)
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
    @Ignore
    public void test13() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5, 7), 0),
                new EProp(3, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(5, $ont.rType$("hasRelation"), Rel.Direction.R, null, 6, 0),
                new Rel(6, $ont.rType$("hasRelation"), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Relation"), $ont.$entity$("Relation").getProperties(), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9, 10), 0),
                new EProp(8, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "own")),
                new EProp(9, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(10, $ont.rType$("hasRvalue"), Rel.Direction.R, null, 11, 0),
                new ETyped(11, "C", $ont.eType$("Rvalue"), $ont.$entity$("Rvalue").getProperties(), 0, 0)
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
    @Ignore
    public void test14() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5, 55, 6), 0),
                new EProp(3, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new EProp(5, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(55, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2")),
                new Rel(6, $ont.rType$("hasRelation"), Rel.Direction.R, null, 7, 0),
                new ETyped(7, "B", $ont.eType$("Relation"), $ont.$entity$("Relation").getProperties(), 8, 0),
                new Quant1(8, QuantType.all, Arrays.asList(9, 10, 11, 12), 0),
                new EProp(9, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(10, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(11, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2")),
                new Rel(12, $ont.rType$("hasRvalue"), Rel.Direction.R, null, 13, 0),
                new ETyped(13, "C", $ont.eType$("Rvalue"), $ont.$entity$("Rvalue").getProperties(), 14, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15, 16, 17), 0),
                new EProp(15, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(16, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(17, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2"))
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
    @Ignore
    public void loadData() throws UnknownHostException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "roman.es").build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        BulkRequestBuilder bulk = client.prepareBulk();

        IndexPartitions entityPartitions = new IndexPartitions.Impl("logicalId",
                new IndexPartitions.Partition.Range.Impl<>("e000", "e300", "e0"),
                new IndexPartitions.Partition.Range.Impl<>("e300", "e600", "e1"),
                new IndexPartitions.Partition.Range.Impl<>("e600", "e999", "e2"));

        IndexPartitions relationPartitions = new IndexPartitions.Impl("relationId",
                new IndexPartitions.Partition.Range.Impl<>("r0000", "r1000", "r0"),
                new IndexPartitions.Partition.Range.Impl<>("r1000", "r2000", "r1"),
                new IndexPartitions.Partition.Range.Impl<>("r2000", "r9999", "r2"));

        Iterable<String> allIndices = Stream.ofAll(entityPartitions.partitions()).appendAll(relationPartitions.partitions())
                .flatMap(IndexPartitions.Partition::indices).distinct().toJavaList();
        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());


        int currentEntityLogicalId = 0;
        int evalueId = 0;
        while(currentEntityLogicalId < 100) {
            for(String context : Arrays.asList("context1", "context2", "global")) {
                String logicalId = "e" + String.format("%03d", currentEntityLogicalId);
                String index = Stream.ofAll(entityPartitions.partitions()).map(partition -> (IndexPartitions.Partition.Range)partition)
                        .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.indices()).get(0)).get(0);
                String category = "person";

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

                if (context.equals("global")) {
                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "title")
                                    .put("bdt", "title")
                                    .put("textValue", UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "description")
                                    .put("bdt", "description")
                                    .put("textValue", UUID.randomUUID().toString() + "\n" +
                                            UUID.randomUUID().toString() + "\n" +
                                            UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                } else {
                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "name")
                                    .put("bdt", "name")
                                    .put("stringValue", UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "name")
                                    .put("bdt", "name")
                                    .put("stringValue", UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", ThreadLocalRandom.current().nextInt(120))
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                }
            }

            currentEntityLogicalId++;
        }

        List<String> colors = Arrays.asList("red", "blue", "green", "white", "black", "brown", "orange", "purple", "pink", "yellow");
        Random random = new Random();
        for(int i = 0 ; i < 20 ; i++, currentEntityLogicalId++) {
            for (String context : Arrays.asList("context1", "context2", "global")) {
                String logicalId = "e" + String.format("%03d", currentEntityLogicalId);
                String index = Stream.ofAll(entityPartitions.partitions()).map(partition -> (IndexPartitions.Partition.Range)partition)
                        .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.indices()).get(0)).get(0);
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

                if (context.equals("global")) {
                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "title")
                                    .put("bdt", "title")
                                    .put("textValue", UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "description")
                                    .put("bdt", "description")
                                    .put("textValue", UUID.randomUUID().toString() + "\n" +
                                            UUID.randomUUID().toString() + "\n" +
                                            UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                } else {
                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "color")
                                    .put("bdt", "color")
                                    .put("stringValue", colors.get(random.nextInt(colors.size())))
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("Evalue").setId("ev" + String.format("%05d", evalueId++))
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("security1", "securityValue1")
                                    .put("security2", "securityValue2")
                                    .put("propertyId", "licenseNumber")
                                    .put("bdt", "licenseNumber")
                                    .put("stringValue", UUID.randomUUID().toString())
                                    .put("lastUpdateUser", UUID.randomUUID().toString())
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                }
            }
        }

        int relationId = 0;
        int rvalueId = 0;
        for(int i = 0 ; i < 20; i++) {
            for (String context : Arrays.asList("context1", "context2")) {
                String relationIdString = "r" + String.format("%04d", relationId++);
                String index = Stream.ofAll(relationPartitions.partitions()).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(relationIdString)).map(partition -> Stream.ofAll(partition.indices()).get(0)).get(0);
                String category = "own";

                String personLogicalId = "e" + String.format("%03d", i);
                String personEntityId = personLogicalId + "." + context;
                String personIndex = Stream.ofAll(entityPartitions.partitions()).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(personLogicalId)).map(partition -> Stream.ofAll(partition.indices()).get(0)).get(0);

                String propertyLogicalId = "e" + String.format("%03d", 200 + i);
                String propertyEntityId = propertyLogicalId + "." + context;
                String propertyCategory = ((i / 5) % 2) == 0 ? "car" : "boat";
                String propertyIndex = Stream.ofAll(entityPartitions.partitions()).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(propertyLogicalId)).map(partition -> Stream.ofAll(partition.indices()).get(0)).get(0);

                String relationLastUpdateUser = UUID.randomUUID().toString();
                String relationLastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
                String relationCreateTime = sdf.format(new Date(System.currentTimeMillis()));

                bulk.add(client.prepareIndex().setIndex(personIndex).setType("Relation.D").setId(relationIdString + "." + Direction.OUT.toString().toLowerCase())
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(personLogicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("entityA", new MapBuilder<String, Object>()
                                        .put("id", personEntityId)
                                        .put("category", "person")
                                        .get())
                                .put("entityB", new MapBuilder<String, Object>()
                                        .put("id", propertyEntityId)
                                        .put("category", propertyCategory)
                                        .get())
                                .put("relationId", relationIdString)
                                .put("direction", Direction.OUT.toString().toLowerCase())
                                .put("context", context)
                                .put("category", category)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(propertyIndex).setType("Relation.D").setId(relationIdString + "." + Direction.IN.toString().toLowerCase())
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(propertyLogicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("entityB", new MapBuilder<String, Object>()
                                        .put("id", personEntityId)
                                        .put("category", "person")
                                        .get())
                                .put("entityA", new MapBuilder<String, Object>()
                                        .put("id", propertyEntityId)
                                        .put("category", propertyCategory)
                                        .get())
                                .put("relationId", relationIdString)
                                .put("direction", Direction.IN.toString().toLowerCase())
                                .put("context", context)
                                .put("category", category)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("Relation.S").setId(relationIdString)
                        .setOpType(IndexRequest.OpType.INDEX)
                        .setSource(new MapBuilder<String, Object>()
                                .put("entityA", new MapBuilder<String, Object>()
                                        .put("id", personEntityId)
                                        .put("category", "person")
                                        .get())
                                .put("entityB", new MapBuilder<String, Object>()
                                        .put("id", propertyEntityId)
                                        .put("category", propertyCategory)
                                        .get())
                                .put("context", context)
                                .put("category", category)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("Rvalue").setId("rv" + String.format("%05d", rvalueId++))
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("propertyId", "since")
                                .put("bdt", "date")
                                .put("dateValue", sdf.format(new Date(System.currentTimeMillis())))
                                .put("lastUpdateUser", UUID.randomUUID().toString())
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("Rvalue").setId("rv" + String.format("%05d", rvalueId++))
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("security1", "securityValue1")
                                .put("security2", "securityValue2")
                                .put("propertyId", "paid")
                                .put("bdt", "payment")
                                .put("intValue", random.nextInt(1000))
                                .put("lastUpdateUser", UUID.randomUUID().toString())
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));
            }
        }

        bulk.execute().actionGet();
    }
}
