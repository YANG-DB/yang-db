package com.kayhut.fuse.services.engine2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

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

        Query query = Query.Builder.instance().withName("query2").withOnt($ont.name()).withElements(Arrays.asList(
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
    public void test_fetchEntityById22() throws IOException, InterruptedException {
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
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12, 13), 0),
                new EProp(10, $ont.pType$("security1"), Constraint.of(ConstraintOp.eq, "securityValue1")),
                new EProp(11, $ont.pType$("security2"), Constraint.of(ConstraintOp.eq, "securityValue2")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(13, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty))

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
    public void test_fetchEntityById3() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("query2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 6), 0),
                new EProp(3, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, null, 7, 0),
                new ETyped(7, "B", $ont.eType$("Evalue"), $ont.$entity$("Evalue").getProperties(), 8, 0),
                new Quant1(8, QuantType.all, Arrays.asList(9, 10), 0),
                new EProp(9, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.notEmpty)),
                new Rel(10, $ont.rType$("hasReference"), Rel.Direction.R, null, 11, 0),
                new ETyped(11, "C", $ont.eType$("Reference"), $ont.$entity$("Reference").getProperties(), 0, 0)
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
    public void test_fetchEntityInsights() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("query2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 6), 0),
                new EProp(3, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e015")),
                new Rel(6, $ont.rType$("hasInsight"), Rel.Direction.R, null, 7, 0),
                new ETyped(7, "B", $ont.eType$("Insight"), $ont.$entity$("Insight").getProperties(), 8, 0),
                new Quant1(8, QuantType.all, Arrays.asList(9, 10), 0),
                new EProp(9, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new Rel(10, $ont.rType$("hasReference"), Rel.Direction.R, null, 11, 0),
                new ETyped(11, "C", $ont.eType$("Reference"), $ont.$entity$("Reference").getProperties(), 0, 0)
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
    public void test_fetchEntityInsights2() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("query2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "1", $ont.eType$("Entity"), $ont.$entity$("Entity").getProperties(), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(15, 18), 0),
                new EProp(18, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.eq, "e000")),
                new Rel(15, $ont.rType$("hasInsight"), Rel.Direction.R, null, 16, 0),
                new ETyped(16, "16", $ont.eType$("Insight"), $ont.$entity$("Insight").getProperties(), 17, 0),
                new Quant1(17, QuantType.all, Arrays.asList(), 0)
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
                new EProp(6, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "color"))
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
    public void loadData() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "roman.es").build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        String workingDir = System.getProperty("user.dir");
        String templatesPath = Paths.get(workingDir, "../", "fuse-assembly", "resources", "indexTemplates").toString();

        File[] templateFiles = new File(templatesPath).listFiles();
        if (templateFiles != null) {
            for (File templateFile : templateFiles) {
                String templateName = FilenameUtils.getBaseName(templateFile.getName());
                String template = FileUtils.readFileToString(templateFile, "utf-8");
                if (!client.admin().indices().getTemplates(new GetIndexTemplatesRequest(templateName)).actionGet().getIndexTemplates().isEmpty()) {
                    client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet();
                }
                client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template)).actionGet();
            }
        }

        List<IndexPartitions.Partition> ePartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("e000", "e300", "e0"),
                new IndexPartitions.Partition.Range.Impl<>("e300", "e600", "e1"),
                new IndexPartitions.Partition.Range.Impl<>("e600", "e999", "e2"));
        String entityIdFormat = "%03d";

        List<IndexPartitions.Partition> relPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("r0000", "r1000", "rel0"),
                new IndexPartitions.Partition.Range.Impl<>("r1000", "r2000", "rel1"),
                new IndexPartitions.Partition.Range.Impl<>("r2000", "r9999", "rel2"));
        String relationIdFormat = "%04d";

        List<IndexPartitions.Partition> refPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("ref00000", "ref00200", "ref0"),
                new IndexPartitions.Partition.Range.Impl<>("ref00200", "ref00400", "ref1"));
        String referenceIdFormat = "%05d";

        List<IndexPartitions.Partition> iPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("i0000", "i0100", "i0"),
                new IndexPartitions.Partition.Range.Impl<>("i0100", "i0200", "i1"));
        String insightIdFormat = "%04d";

        Iterable<String> allIndices = Stream.ofAll(ePartitions)
                .appendAll(relPartitions)
                .appendAll(refPartitions)
                .appendAll(iPartitions)
                .flatMap(IndexPartitions.Partition::getIndices).distinct().toJavaList();
        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());


        int currentEntityLogicalId = 0;
        int evalueId = 0;

        Random random = new Random();

        List<String> contexts = Arrays.asList("context1", "context2", "global");
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
            String referenceId = "ref" + String.format(referenceIdFormat, refId);
            String index = Stream.ofAll(refPartitions).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(index).setType("reference").setId(referenceId)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(new MapBuilder<String, Object>()
                            .put("url", "http://" + UUID.randomUUID().toString() + "." + domains.get(random.nextInt(domains.size())))
                            .put("content", contents.get(random.nextInt(contents.size())))
                            .put("system", "system" + random.nextInt(10))
                            .put("agency", "agency" + random.nextInt(10))
                            .put("security1", "securityValue1")
                            .put("security2", "securityValue2")
                            .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                            .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                            .put("creationUser", users.get(random.nextInt(users.size())))
                            .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));
        }
        bulk.execute().actionGet();

        bulk = client.prepareBulk();
        while (currentEntityLogicalId < 100) {
            for (String context : contexts) {
                String logicalId = "e" + String.format(entityIdFormat, currentEntityLogicalId);
                String index = Stream.ofAll(ePartitions).map(partition -> (IndexPartitions.Partition.Range) partition)
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

                bulk.add(client.prepareIndex().setIndex(index).setType("entity").setId(logicalId + "." + context)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("logicalId", logicalId)
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                if (context.equals("global")) {
                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "title")
                                    .put("bdt", "title")
                                    .put("textValue", users.get(currentEntityLogicalId))
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "description")
                                    .put("bdt", "description")
                                    .put("textValue", description)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    for (String personNickname : personNicknames) {
                        bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                                .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                                .setSource(new MapBuilder<String, Object>()
                                        .put("logicalId", logicalId)
                                        .put("entityId", logicalId + "." + context)
                                        .put("context", context)
                                        .put("authorization", "source1.procedure1")
                                        .put("authorizationCount", 1)
                                        .put("fieldId", "nicknames")
                                        .put("bdt", "nicknames")
                                        .put("textValue", personNickname)
                                        .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                                .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                                .toJavaList())
                                        .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                        .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                        .put("creationUser", users.get(random.nextInt(users.size())))
                                        .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                        .get()));
                    }
                } else {
                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "name")
                                    .put("bdt", "name")
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("stringValue", users.get(currentEntityLogicalId))
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    int age = random.nextInt(120);
                    int anotherAge = age + (random.nextInt(8) - 4);

                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", age)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", anotherAge)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "age")
                                    .put("bdt", "age")
                                    .put("intValue", anotherAge)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
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
        bulk.execute().actionGet();

        bulk = client.prepareBulk();
        List<String> colors = Arrays.asList("red", "blue", "green", "white", "black", "brown", "orange", "purple", "pink", "yellow");
        for (int i = 0; i < 20; i++, currentEntityLogicalId++) {
            for (String context : contexts) {
                String logicalId = "e" + String.format(entityIdFormat, currentEntityLogicalId);
                String index = Stream.ofAll(ePartitions).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                String category = ((i / 5) % 2) == 0 ? "car" : "boat";
                String color = colors.get(random.nextInt(colors.size()));
                String title = color + " " + category;
                String description = title;

                bulk.add(client.prepareIndex().setIndex(index).setType("entity").setId(logicalId + "." + context)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("logicalId", logicalId)
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                if (context.equals("global")) {
                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "title")
                                    .put("bdt", "title")
                                    .put("textValue", title)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "description")
                                    .put("bdt", "description")
                                    .put("textValue", description)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                } else {
                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "color")
                                    .put("bdt", "color")
                                    .put("stringValue", color)
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));

                    bulk.add(client.prepareIndex().setIndex(index).setType("e.value").setId("ev" + evalueId++)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("logicalId", logicalId)
                                    .put("entityId", logicalId + "." + context)
                                    .put("context", context)
                                    .put("authorization", "source1.procedure1")
                                    .put("authorizationCount", 1)
                                    .put("fieldId", "licenseNumber")
                                    .put("bdt", "licenseNumber")
                                    .put("stringValue", UUID.randomUUID().toString().substring(0, 8))
                                    .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                            .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                            .toJavaList())
                                    .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                    .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .put("creationUser", users.get(random.nextInt(users.size())))
                                    .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                    .get()));
                }
            }
        }
        bulk.execute().actionGet();

        bulk = client.prepareBulk();
        int relationId = 0;
        int rvalueId = 0;
        for (int i = 0; i < 20; i++) {
            for (String context : Arrays.asList("context1", "context2")) {
                String relationIdString = "r" + String.format(relationIdFormat, relationId++);
                String index = Stream.ofAll(relPartitions).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(relationIdString)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                String category = "own";

                String personLogicalId = "e" + String.format(entityIdFormat, i);
                String personEntityId = personLogicalId + "." + context;
                String personIndex = Stream.ofAll(ePartitions).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(personLogicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

                String propertyLogicalId = "e" + String.format(entityIdFormat, 200 + i);
                String propertyEntityId = propertyLogicalId + "." + context;
                String propertyCategory = ((i / 5) % 2) == 0 ? "car" : "boat";
                String propertyIndex = Stream.ofAll(ePartitions).map(partition -> (IndexPartitions.Partition.Range) partition)
                        .filter(partition -> partition.isWithin(propertyLogicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

                String relationLastUpdateUser = users.get(random.nextInt(users.size()));
                String relationCreationUser = users.get(random.nextInt(users.size()));
                String relationLastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
                String relationCreateTime = sdf.format(new Date(System.currentTimeMillis()));

                bulk.add(client.prepareIndex().setIndex(personIndex).setType("e.relation").setId(relationIdString + ".out")
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(personLogicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("entityAId", personEntityId)
                                .put("entityACategory", "person")
                                .put("entityBId", propertyEntityId)
                                .put("entityBCategory", propertyCategory)
                                .put("relationId", relationIdString)
                                .put("direction", "out")
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationUser", relationCreationUser)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(propertyIndex).setType("e.relation").setId(relationIdString + ".in")
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(propertyLogicalId)
                        .setSource(new MapBuilder<String, Object>()
                                .put("entityBId", personEntityId)
                                .put("entityBCategory", "person")
                                .put("entityAId", propertyEntityId)
                                .put("entityACategory", propertyCategory)
                                .put("relationId", relationIdString)
                                .put("direction", "in")
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationUser", relationCreationUser)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("relation").setId(relationIdString)
                        .setOpType(IndexRequest.OpType.INDEX)
                        .setSource(new MapBuilder<String, Object>()
                                .put("entityAId", personEntityId)
                                .put("entityACategory", "person")
                                .put("entityBId", propertyEntityId)
                                .put("entityBCategory", propertyCategory)
                                .put("context", context)
                                .put("category", category)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", relationLastUpdateUser)
                                .put("lastUpdateTime", relationLastUpdateTime)
                                .put("creationUser", relationCreationUser)
                                .put("creationTime", relationCreateTime).get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("r.value").setId("rv" + rvalueId++)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("fieldId", "since")
                                .put("bdt", "date")
                                .put("dateValue", sdf.format(new Date(System.currentTimeMillis())))
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));

                bulk.add(client.prepareIndex().setIndex(index).setType("r.value").setId("rv" + rvalueId++)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(relationIdString)
                        .setSource(new MapBuilder<String, Object>()
                                .put("relationId", relationIdString)
                                .put("context", context)
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("fieldId", "paid")
                                .put("bdt", "payment")
                                .put("intValue", random.nextInt(1000))
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                        .toJavaList())
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis())))
                                .get()));
            }
        }
        bulk.execute().actionGet();

        bulk = client.prepareBulk();
        int iId = 0;
        for (int entityId = 0; entityId < 100; entityId++) {
            List<String> logicalIds = Stream.ofAll(Arrays.asList(
                    entityId, (entityId + 1) % 100, (entityId + 2) % 100, (entityId + 3) % 100))
                    .map(id -> "e" + String.format(entityIdFormat, id))
                    .toJavaList();

            for (String context : Stream.ofAll(contexts).filter(context -> !context.equals("global"))) {
                String insightId = "i" + String.format(insightIdFormat, iId++);
                String index = Stream.ofAll(iPartitions).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                        .filter(partition -> partition.isWithin(insightId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);


                bulk.add(client.prepareIndex().setIndex(index).setType("insight").setId(insightId)
                        .setOpType(IndexRequest.OpType.INDEX)
                        .setSource(new MapBuilder<String, Object>()
                                .put("content", contents.get(random.nextInt(contents.size())))
                                .put("context", context)
                                .put("entityIds", Stream.ofAll(logicalIds).map(logicalId -> logicalId + "." + context).toJavaList())
                                .put("refs", Stream.ofAll(Arrays.asList(random.nextInt(400), random.nextInt(400), random.nextInt(400), random.nextInt(400)))
                                        .distinct().take(random.nextInt(2) + 1).map(refId -> "ref" + String.format(referenceIdFormat, refId))
                                        .toJavaList())
                                .put("authorization", "source1.procedure1")
                                .put("authorizationCount", 1)
                                .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                                .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                                .put("creationUser", users.get(random.nextInt(users.size())))
                                .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));

                for (String logicalId : logicalIds) {
                    String logicalEntityIndex =
                            Stream.ofAll(ePartitions).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                                    .filter(partition -> partition.isWithin(logicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

                    bulk.add(client.prepareIndex().setIndex(logicalEntityIndex).setType("e.insight").setId(logicalId + "." + insightId)
                            .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                            .setSource(new MapBuilder<String, Object>()
                                    .put("entityId", logicalId + "." + context)
                                    .put("insightId", insightId).get()));
                }
            }

        }
        bulk.execute().actionGet();
    }
}
