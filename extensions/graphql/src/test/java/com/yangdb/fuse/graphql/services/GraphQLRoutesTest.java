package com.yangdb.fuse.graphql.services;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.extension.QueryLanguages;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.graphql.GraphQLExtension;
import com.yangdb.fuse.graphql.cursor.CreateGraphQLCursorRequest;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.services.appRegistrars.LoggingJacksonRendererRegistrar;
import com.yangdb.fuse.services.appRegistrars.QueryLanguageRoutesRegistrar;
import com.yangdb.fuse.services.controllers.QueryController;
import com.yangdb.fuse.services.extensions.YangDbExtensions;
import com.yangdb.fuse.services.modules.LoggingJacksonModule;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.resource;
import static com.yangdb.fuse.model.transport.Status.NOT_FOUND;
import static com.yangdb.fuse.model.transport.Status.OK;

/**
 * The GraphQL extension over HTTP, wired as the service wires it (ServiceLoader discovery, its module and registrar,
 * then the generic language routes), with the Dragons ontology registered from JSON. The query controller translates
 * instead of executing (execution needs OpenSearch: the e2e suite, docker/e2e/doc_queries.py, covers it).
 */
public class GraphQLRoutesTest {
    private static final AppUrlSupplier URLS = new DefaultAppUrlSupplier("/fuse");
    private static final TypeLiteral<Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>>> LANGUAGES =
            new TypeLiteral<Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>>>() {};
    private static Jooby app;
    private static int port;

    /** run / create translate the query with the language registry and answer with the ASG query's print */
    private static QueryController translatingController(Jooby app) {
        return (QueryController) Proxy.newProxyInstance(QueryController.class.getClassLoader(), new Class<?>[]{QueryController.class},
                (proxy, method, args) -> {
                    Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>> languages = app.require(Key.get(LANGUAGES));
                    if (method.getName().equals("run") && args.length == 5) {
                        String query = (String) args[0], type = (String) args[1], ontology = (String) args[2];
                        // the service's factory matches the language ignoring case (/fuse/query/graphql/run)
                        String language = languages.keySet().stream().filter(type::equalsIgnoreCase).findFirst().orElse(type);
                        AsgQuery asg = languages.get(language).transform(new QueryInfo<>(query, "q", type, ontology));
                        Map<String, Object> data = new LinkedHashMap<>();
                        data.put("language", type);
                        data.put("asg", AsgQueryDescriptor.print(asg));
                        return ContentResponse.Builder.builder(OK, NOT_FOUND).data((Object) data).compose();
                    }
                    if (method.getName().equals("create") && args[0] instanceof CreateJsonQueryRequest) {
                        CreateJsonQueryRequest request = (CreateJsonQueryRequest) args[0];
                        AsgQuery asg = languages.get(request.getType()).transform(
                                new QueryInfo<>(request.getQuery(), request.getName(), request.getType(), request.getOntology()));
                        return ContentResponse.Builder.builder(OK, NOT_FOUND)
                                .data(new QueryResourceInfo(null, "/fuse/query/" + request.getName(), request.getType(),
                                        AsgQueryDescriptor.print(asg)))
                                .compose();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @BeforeClass
    public static void start() throws Exception {
        Ontology dragons = new ObjectMapper().readValue(resource("ontology/Dragons.json"), Ontology.class);
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        app = new Jooby();
        app.port(port);
        app.use(ConfigFactory.empty()
                .withValue("application.profile", ConfigValueFactory.fromAnyRef("test"))
                .withValue("modules.test", ConfigValueFactory.fromIterable(
                        Collections.singletonList(LoggingJacksonModule.class.getName()))));
        new LoggingJacksonRendererRegistrar(new MetricRegistry()).register(app, URLS);
        // the service's side: the language registry, an ontology store, a query controller, request suppliers
        app.use((env, conf, binder) -> {
            QueryLanguages.mapBinder(binder);
            binder.bind(OntologyProvider.class).toInstance(
                    com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.provider(dragons));
            binder.bind(QueryController.class).toInstance(translatingController(app));
            binder.bind(RequestIdSupplier.class).toInstance(() -> "test");
            binder.bind(RequestExternalMetadataSupplier.class).toInstance(() -> null);
        });
        // what FuseApp does with extensions, in the same order
        List<YangDbExtension> extensions = YangDbExtensions.load(GraphQLRoutesTest.class.getClassLoader());
        YangDbExtensions.installModules(app, extensions);
        YangDbExtensions.registerRoutes(app, URLS, extensions, new HashSet<>());
        new QueryLanguageRoutesRegistrar().register(app, URLS);
        app.start("server.join=false");
        awaitServer();
    }

    @AfterClass
    public static void stop() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    public void theExtensionIsDiscovered() {
        Assert.assertTrue(YangDbExtensions.load(getClass().getClassLoader()).stream().anyMatch(e -> e instanceof GraphQLExtension));
        Assert.assertTrue(app.require(Key.get(LANGUAGES)).containsKey(CreateQueryRequestMetadata.TYPE_GRAPHQL));
        Set<CompositeCursorFactory.Binding> cursors = app.require(Key.get(new TypeLiteral<Set<CompositeCursorFactory.Binding>>() {}));
        Assert.assertTrue(cursors.stream().anyMatch(b -> CreateGraphQLCursorRequest.CursorType.equals(b.getType())));
    }

    // G1, through REST: the old and the generic run routes

    @Test
    public void runsAGraphQLQueryOnTheDragonsOntology() throws Exception {
        String q = URLEncoder.encode("{ dragon { name color } }", StandardCharsets.UTF_8);
        for (String path : Arrays.asList("/fuse/query/graphQL/run?ontology=Dragons&graphQL=" + q,
                "/fuse/query/graphQL/run?ontology=Dragons&query=" + q,
                "/fuse/query/graphql/run?ontology=Dragons&query=" + q)) {
            for (String method : Arrays.asList("GET", "POST")) {
                Reply reply = call(method, path, null);
                Assert.assertEquals(method + " " + path + ": " + reply.body, 200, reply.status);
                JsonNode data = new ObjectMapper().readTree(reply.body).path("data");
                Assert.assertTrue(reply.body, "graphQL".equalsIgnoreCase(data.path("language").asText()));
                Assert.assertTrue(reply.body, data.path("asg").asText().contains("Dragon"));
            }
        }
    }

    @Test
    public void runsAGraphQLQueryFromTheBody() throws Exception {
        Reply reply = call("POST", "/fuse/query/graphQL/run?ontology=Dragons", "{ horse { name } }");
        Assert.assertEquals(reply.body, 200, reply.status);
        Assert.assertTrue(reply.body, new ObjectMapper().readTree(reply.body).path("data").path("asg").asText().contains("Horse"));
    }

    @Test
    public void createsAGraphQLQuery() throws Exception {
        Reply reply = call("POST", "/fuse/query/graphQL",
                "{\"id\":\"q1\",\"name\":\"q1\",\"query\":\"{ dragon { name } }\",\"ontology\":\"Dragons\"}");
        Assert.assertEquals(reply.body, 200, reply.status);
        Assert.assertTrue(reply.body, reply.body.contains("Dragon"));
    }

    // G2 and G3, through REST: schema import

    @Test
    public void importsASchemaTwice() throws Exception {
        String sdl = resource("graphql/starWars.graphql") + "\n" + resource("graphql/whereSchema.graphql");
        for (int i = 0; i < 2; i++) {
            Reply reply = call("POST", "/fuse/graphql/ontology/StarWars", sdl);
            Assert.assertEquals("import " + (i + 1) + ": " + reply.body, 200, reply.status);
            JsonNode data = new ObjectMapper().readTree(reply.body).path("data");
            Assert.assertEquals(reply.body, "StarWars", data.path("ont").asText());
            Assert.assertTrue(reply.body, data.path("entityTypes").toString().contains("Human"));
        }
    }

    @Test
    public void importsASchemaWithoutTheWhereClauseTypes() throws Exception {
        Reply reply = call("POST", "/fuse/graphql/ontology/StarWars", resource("graphql/starWars.graphql"));
        Assert.assertEquals(reply.body, 200, reply.status);
        Assert.assertTrue(reply.body, new ObjectMapper().readTree(reply.body).path("data").path("entityTypes").toString().contains("Droid"));
    }

    @Test
    public void aBrokenSchemaGetsAClearError() throws Exception {
        Reply reply = call("POST", "/fuse/graphql/ontology/Broken", "type Query { broken: Missing }");
        Assert.assertEquals(reply.body, 400, reply.status);
        Assert.assertTrue(reply.body, reply.body.contains("Missing"));
    }

    private static void awaitServer() throws Exception {
        long deadline = System.currentTimeMillis() + 30_000;
        while (true) {
            try {
                call("GET", "/fuse/query/graphQL/run", null);
                return;
            } catch (IOException notReadyYet) {
                if (System.currentTimeMillis() > deadline) {
                    throw new IllegalStateException("test server did not start on port " + port, notReadyYet);
                }
            }
            Thread.sleep(100);
        }
    }

    private static Reply call(String method, String path, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + port + path).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");
        if (body != null || method.equals("POST")) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", body != null && body.startsWith("{\"") ? "application/json" : "text/plain");
            try (OutputStream out = connection.getOutputStream()) {
                out.write((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
            }
        }
        Reply reply = new Reply();
        reply.status = connection.getResponseCode();
        InputStream in = reply.status < 400 ? connection.getInputStream() : connection.getErrorStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (in != null) {
            try (InputStream stream = in) {
                stream.transferTo(buffer);
            }
        }
        reply.body = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        connection.disconnect();
        return reply;
    }

    private static class Reply {
        int status;
        String body;
    }
}
