package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;

import static com.kayhut.test.data.DragonsOntology.*;
import static java.util.Collections.singletonList;

/**
 * Created by Roman on 07/06/2017.
 */
public class RealClusterTest {
    @Before
    public void setup() throws IOException {
        fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));
    }

    @Test
    @Ignore
    public void test1() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test2() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 2, 0),
                new Rel(2, $ont.rType$(FREEZE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test3() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, Integer.toString(NAME.type), Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test4() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, Integer.toString(NAME.type), Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 6, 5),
                new RelProp(5, Integer.toString(TIMESTAMP.type), Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-04-05 00:00:00.000").getTime(), sdf.parse("2000-05-05 00:00:00.000").getTime())), 0),
                new ETyped(6, "B", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 7, 0),
                new EProp(7, Integer.toString(NAME.type), Constraint.of(ConstraintOp.eq, "gideon"))
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test5() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, Integer.toString(NAME.type), Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 6, 5),
                new RelProp(5, Integer.toString(TIMESTAMP.type), Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-04-05 00:00:00.000").getTime(), sdf.parse("2000-05-05 00:00:00.000").getTime())), 0),
                new ETyped(6, "B", $ont.eType$(DRAGON.name), singletonList(Integer.toString(NAME.type)), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                new EProp(8, Integer.toString(NAME.type), Constraint.of(ConstraintOp.eq, "gideon")),
                new Rel(9, FREEZE.getrType(), Rel.Direction.L, null, 11, 10),
                new RelProp(10, Integer.toString(START_DATE.type),
                        Constraint.of(ConstraintOp.lt, sdf.parse("2000-07-01 00:00:00.000").getTime()), 0),
                new ETyped(11, "C", $ont.eType$(DRAGON.name),
                        Arrays.asList(Integer.toString(NAME.type), Integer.toString(POWER.type)), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());

        long elapsed = System.currentTimeMillis() - start;
        System.out.println(elapsed);
    }


    //region Fields
    private static FuseClient fuseClient;
    private static Ontology.Accessor $ont;
    //endregion
}