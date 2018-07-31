package com.kayhut.fuse.client;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncRestFuseClientTest {
    @Test
    @Ignore
    public void test1() throws Exception {
        AsyncFuseClient fuseClient = new AsyncRestFuseClient("http://localhost:8888/fuse");
        Ontology ontology = fuseClient.getOntology("Knowledge").send().get().getData();
        Ontology.Accessor $ont = new Ontology.Accessor(ontology);

        Query query = Query.Builder.instance().withName("name1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "SE", $ont.eType$("Entity"), 2, 0)))
                .build();

        AtomicBoolean isComplete = new AtomicBoolean(false);

        System.out.println("Before sending query::  ThreadName: " + Thread.currentThread().getName());
        fuseClient.postQuery(new CreateQueryRequest("id1", "name1", query))
                .onSuccess(queryResponse -> {
                    System.out.println("Before sending cursor::  ThreadName: " + Thread.currentThread().getName());
                    fuseClient.postCursor(queryResponse.getData(), new CreatePathsCursorRequest())
                            .onSuccess(cursorResponse -> {
                                System.out.println("Before sending page::  ThreadName: " + Thread.currentThread().getName());
                                fuseClient.postPage(cursorResponse.getData(), new CreatePageRequest(10))
                                        .onSuccess(pageResponse -> {
                                            System.out.println("Before getting page data::  ThreadName: " + Thread.currentThread().getName());
                                            fuseClient.getPageData(pageResponse.getData())
                                                    .onSuccess(pageDataResponse -> {
                                                        System.out.println("After getting page data::  ThreadName: " + Thread.currentThread().getName());
                                                        isComplete.set(true);
                                                    }).send();
                                        }).send();
                            }).send();
                }).send();

        while(!isComplete.get()) {
            Thread.sleep(1000);
        }

        int x = 5;
    }
}
