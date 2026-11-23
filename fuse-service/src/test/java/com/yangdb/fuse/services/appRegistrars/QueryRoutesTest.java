package com.yangdb.fuse.services.appRegistrars;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.services.controllers.QueryController;
import com.yangdb.fuse.services.modules.LoggingJacksonModule;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.yangdb.fuse.model.transport.Status.NOT_FOUND;
import static com.yangdb.fuse.model.transport.Status.OK;

/**
 * The fixed query routes of {@link QueryControllerRegistrar} over HTTP, registered as the service registers them
 * (before the generic {@link QueryLanguageRoutesRegistrar} routes), with a query controller that records what it is
 * called with instead of planning and executing (execution needs OpenSearch).
 */
public class QueryRoutesTest {
    private static final AppUrlSupplier URLS = new DefaultAppUrlSupplier("/fuse");
    private static final List<String> CALLS = Collections.synchronizedList(new ArrayList<>());
    private static Jooby app;
    private static int port;

    private static QueryController recordingController() {
        return (QueryController) Proxy.newProxyInstance(QueryController.class.getClassLoader(), new Class<?>[]{QueryController.class},
                (proxy, method, args) -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("method", method.getName());
                    switch (method.getName()) {
                        case "runV1Query":
                            CALLS.add("runV1Query " + ((Query) args[0]).getOnt());
                            return ContentResponse.Builder.builder(OK, NOT_FOUND).data((Object) data).compose();
                        case "create":
                            if (args[0] instanceof CreateJsonQueryRequest) {
                                CreateJsonQueryRequest request = (CreateJsonQueryRequest) args[0];
                                CALLS.add("create " + request.getType());
                                return ContentResponse.Builder.builder(OK, NOT_FOUND)
                                        .data(new QueryResourceInfo(null, "/fuse/query/" + request.getId(), request.getId(), null))
                                        .compose();
                            }
                            break;
                        case "run":
                        case "runSparql":
                        case "runCypher":
                            CALLS.add(method.getName() + " " + Arrays.toString(args));
                            return ContentResponse.Builder.builder(OK, NOT_FOUND).data((Object) data).compose();
                        default:
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
        QueryController controller = recordingController();
        app.use((env, conf, binder) -> {
            binder.bind(QueryController.class).toInstance(controller);
            binder.bind(RequestIdSupplier.class).toInstance(() -> "test");
            binder.bind(RequestExternalMetadataSupplier.class).toInstance(() -> null);
        });
        // the service's order: the fixed routes, then the generic language routes
        new QueryControllerRegistrar().register(app, URLS);
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

    @Before
    public void clear() {
        CALLS.clear();
    }

    /** A9: the fixed /v1/run route answers itself instead of falling through to /:lang/run */
    @Test
    public void runsAV1Query() throws Exception {
        Reply reply = call("POST", "/fuse/query/v1/run", "{\"ont\":\"Dragons\",\"name\":\"q\",\"elements\":[]}");
        Assert.assertEquals(reply.body, 200, reply.status);
        Assert.assertEquals(reply.body, "runV1Query", new ObjectMapper().readTree(reply.body).path("data").path("method").asText());
        Assert.assertEquals(Collections.singletonList("runV1Query Dragons"), CALLS);
    }

    private static void awaitServer() throws Exception {
        long deadline = System.currentTimeMillis() + 30_000;
        while (true) {
            try {
                call("GET", "/fuse/query/none/elastic/view", null);
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
        connection.setInstanceFollowRedirects(false);
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
