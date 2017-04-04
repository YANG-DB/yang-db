package com.kayhut.fuse.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.TestUtils.ContentMatcher;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static com.kayhut.fuse.services.TestUtils.loadOntology;
import static io.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CatalogTest {

    @ClassRule
    public static JoobyRule createApp() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getNextResults(anyInt())).thenReturn(new QueryResult());

        CursorFactory cursorFactory = mock(CursorFactory.class);
        when(cursorFactory.createCursor(any())).thenReturn(cursor);

        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.mockEngine.dev.conf")
                .injector((stage, module) -> {
                    return Guice.createInjector(stage, Modules.override(module).with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(CursorFactory.class).toInstance(cursorFactory);
                        }
                    }));
                }));
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalog() throws IOException {
        Ontology ontology = loadOntology("Dragons.json");
        given()
                .contentType("application/json")
                .get("/fuse/catalog/ontology/Dragons")
                .then()
                .assertThat()
                .body(new ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        return new ObjectMapper().writeValueAsString(contentResponse.getData()).equals(expected);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

}

