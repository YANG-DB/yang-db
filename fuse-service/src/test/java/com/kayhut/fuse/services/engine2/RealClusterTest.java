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
import javaslang.collection.Stream;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static java.util.Collections.singletonList;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class RealClusterTest {
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
}
