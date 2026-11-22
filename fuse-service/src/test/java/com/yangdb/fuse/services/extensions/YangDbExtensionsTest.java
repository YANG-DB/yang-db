package com.yangdb.fuse.services.extensions;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.yangdb.fuse.asg.translator.BasicJsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.extension.QueryLanguages;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.extension.hello.HelloWorldExtension;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;
import com.yangdb.fuse.services.appRegistrars.LoggingJacksonRendererRegistrar;
import com.yangdb.fuse.services.appRegistrars.QueryLanguageRoutesRegistrar;
import com.yangdb.fuse.services.controllers.QueryController;
import com.yangdb.fuse.services.modules.LoggingJacksonModule;
import org.jooby.Jooby;
import org.jooby.Route;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.yangdb.fuse.model.transport.Status.NOT_FOUND;
import static com.yangdb.fuse.model.transport.Status.OK;

/**
 * The extension SPI end to end, with the hello-world extension of the test classpath
 * ({@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension}): it is discovered, its language and route are
 * reachable over HTTP, and nothing in it is configured anywhere.
 */
public class YangDbExtensionsTest {
    private static final AppUrlSupplier URLS = new DefaultAppUrlSupplier("/fuse");
    private static Jooby app;
    private static int port;

    /**
     * A query controller whose run/create translate the query with the real language registry (the factory the
     * service's driver uses) and answer with what was translated, instead of planning and executing it.
     */
    private static QueryController translatingController(Jooby app) {
        return (QueryController) Proxy.newProxyInstance(QueryController.class.getClassLoader(), new Class<?>[]{QueryController.class},
                (proxy, method, args) -> {
                    JsonQueryTransformerFactory factory = app.require(JsonQueryTransformerFactory.class);
                    if (method.getName().equals("run") && args.length == 5) {
                        String query = (String) args[0], type = (String) args[1], ontology = (String) args[2];
                        AsgQuery asg = factory.transform(type).transform(new QueryInfo<>(query, "q", type, ontology));
                        Map<String, Object> data = new LinkedHashMap<>();
                        data.put("language", type);
                        data.put("asg", asg.getName());
                        data.put("ontology", asg.getOnt());
                        return ContentResponse.Builder.builder(OK, NOT_FOUND).data((Object) data).compose();
                    }
                    if (method.getName().equals("create") && args[0] instanceof CreateJsonQueryRequest) {
                        CreateJsonQueryRequest request = (CreateJsonQueryRequest) args[0];
                        AsgQuery asg = factory.transform(request.getType()).transform(
                                new QueryInfo<>(request.getQuery(), request.getName(), request.getType(), request.getOntology()));
                        return ContentResponse.Builder.builder(OK, NOT_FOUND)
                                .data(new QueryResourceInfo(null, "/fuse/query/" + asg.getName(), request.getType(), null))
                                .compose();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @BeforeClass
    public static void start() throws Exception {
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
        // the service's side: the language registry (M2AsgModule) and a query controller
        app.use((env, conf, binder) -> {
            QueryLanguages.mapBinder(binder);
            binder.bind(JsonQueryTransformerFactory.class).to(BasicJsonQueryTransformerFactory.class);
            binder.bind(QueryController.class).toInstance(translatingController(app));
        });
        // a fixed route under the query store, registered before the generic ones as the service's are
        app.post(URLS.queryStoreUrl() + "/v1/run", () -> "fixed v1 route");

        // what FuseApp does with extensions, in the same order
        List<YangDbExtension> extensions = YangDbExtensions.load(YangDbExtensionsTest.class.getClassLoader());
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
    public void serviceLoaderFindsTheHelloWorldExtension() {
        List<YangDbExtension> extensions = YangDbExtensions.load(getClass().getClassLoader());
        Assert.assertTrue(String.valueOf(extensions),
                extensions.stream().anyMatch(e -> e instanceof HelloWorldExtension));
    }

    @Test
    public void theExtensionRouteIsReachable() throws Exception {
        Reply reply = call("GET", "/hello", null);
        Assert.assertEquals(200, reply.status);
        Assert.assertEquals("hello from an extension", new ObjectMapper().readTree(reply.body).asText());
    }

    @Test
    public void theExtensionLanguageRunsThroughTheGenericRunRoute() throws Exception {
        for (String method : Arrays.asList("GET", "POST")) {
            Reply reply = call(method, "/fuse/query/echo/run?query=hello&ontology=Dragons", null);
            Assert.assertEquals(reply.body, 200, reply.status);
            JsonNode data = new ObjectMapper().readTree(reply.body).path("data");
            Assert.assertEquals(reply.body, "echo", data.path("language").asText());
            Assert.assertEquals(reply.body, "hello", data.path("asg").asText());
            Assert.assertEquals(reply.body, "Dragons", data.path("ontology").asText());
        }
    }

    @Test
    public void theRunRouteTakesTheQueryFromTheBodyAndMatchesTheLanguageIgnoringCase() throws Exception {
        Reply reply = call("POST", "/fuse/query/ECHO/run?ontology=Dragons", "from the body");
        Assert.assertEquals(reply.body, 200, reply.status);
        JsonNode data = new ObjectMapper().readTree(reply.body).path("data");
        Assert.assertEquals(reply.body, "echo", data.path("language").asText());
        Assert.assertEquals(reply.body, "from the body", data.path("asg").asText());
    }

    @Test
    public void theExtensionLanguageCreatesQueriesThroughTheGenericPostRoute() throws Exception {
        // the body names no type: the URL's language is used
        Reply reply = call("POST", "/fuse/query/echo", "{\"id\":\"q1\",\"name\":\"q1\",\"query\":\"hi\",\"ontology\":\"Dragons\"}");
        Assert.assertEquals(reply.body, 200, reply.status);
        JsonNode data = new ObjectMapper().readTree(reply.body).path("data");
        Assert.assertEquals(reply.body, "/fuse/query/hi", data.path("resourceUrl").asText());
        Assert.assertEquals(reply.body, "echo", data.path("resourceId").asText());
    }

    @Test
    public void anUnknownLanguageIsA404ListingTheAvailableOnes() throws Exception {
        for (String[] call : new String[][]{{"GET", "/fuse/query/klingon/run?query=x&ontology=Dragons"},
                {"POST", "/fuse/query/klingon/run?query=x&ontology=Dragons"}, {"POST", "/fuse/query/klingon"}}) {
            Reply reply = call(call[0], call[1], call[0].equals("POST") ? "{}" : null);
            Assert.assertEquals(call[1] + ": " + reply.body, 404, reply.status);
            JsonNode error = new ObjectMapper().readTree(reply.body);
            Assert.assertEquals(QueryLanguageRoutesRegistrar.UNKNOWN_LANGUAGE, error.path("errorCode").asText());
            Assert.assertTrue(reply.body, error.path("errorDescription").asText().contains("klingon"));
            Assert.assertTrue(reply.body, error.path("languages").toString().contains("\"echo\""));
        }
    }

    @Test
    public void fixedRoutesRegisteredEarlierWinOverTheGenericOnes() throws Exception {
        Reply reply = call("POST", "/fuse/query/v1/run", "{}");
        Assert.assertEquals(200, reply.status);
        Assert.assertEquals("fixed v1 route", new ObjectMapper().readTree(reply.body).asText());
    }

    @Test
    public void fuseAppRegistersExtensionRoutesExactlyOnce() {
        int before = HelloWorldExtension.HelloRegistrar.REGISTRATIONS.get();
        FuseApp fuseApp = new FuseApp(URLS);
        Assert.assertEquals(before + 1, HelloWorldExtension.HelloRegistrar.REGISTRATIONS.get());

        // the service's route table (without starting it): the extension route once, the generic language routes last
        List<String> routes = new ArrayList<>();
        for (Route.Definition route : Jooby.exportRoutes(fuseApp)) {
            routes.add(route.method() + " " + route.pattern());
        }
        Assert.assertEquals(routes.toString(), 1, Collections.frequency(routes, "GET /hello"));
        Assert.assertEquals(routes.toString(), Arrays.asList(
                "POST /fuse/query/:lang", "GET /fuse/query/:lang/run", "POST /fuse/query/:lang/run"),
                routes.subList(routes.size() - 3, routes.size()));
        Assert.assertTrue(routes.toString(), routes.indexOf("POST /fuse/query/v1/run") < routes.indexOf("POST /fuse/query/:lang/run"));
        Assert.assertTrue(routes.toString(), routes.contains("GET /fuse/query/cypher/run"));
    }

    @Test
    public void aRegistrarClassAlreadyRegisteredIsSkipped() {
        AppRegistrar registrar = (jooby, urls) -> { };
        YangDbExtension twice = new YangDbExtension() {
            @Override
            public List<AppRegistrar> registrars() {
                return Arrays.asList(registrar, registrar);
            }
        };
        Set<Class<?>> registered = new HashSet<>();
        Assert.assertEquals(1, YangDbExtensions.registerRoutes(new Jooby(), URLS, Collections.singletonList(twice), registered).size());
        Assert.assertEquals(0, YangDbExtensions.registerRoutes(new Jooby(), URLS, Collections.singletonList(twice), registered).size());
    }

    private static void awaitServer() throws Exception {
        long deadline = System.currentTimeMillis() + 30_000;
        while (true) {
            try {
                if (call("GET", "/hello", null).status == 200) {
                    return;
                }
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
            connection.setRequestProperty("Content-Type", body != null && body.startsWith("{") ? "application/json" : "text/plain");
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
