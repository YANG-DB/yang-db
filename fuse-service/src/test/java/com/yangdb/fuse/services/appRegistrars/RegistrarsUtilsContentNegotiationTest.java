package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.modules.LoggingJacksonModule;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.jooby.Jooby;
import org.jooby.Results;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

import static com.yangdb.fuse.model.transport.Status.NOT_FOUND;
import static com.yangdb.fuse.model.transport.Status.OK;

/**
 * Content negotiation of the REST layer (KNOWN_ISSUES A1): clients that send no Accept header, or curl's default
 * {@code Accept: *}{@code /*}, must get JSON, while explicitly requested formats keep working.
 * Runs a minimal Jooby app with the service's real renderer registrar and response helper.
 */
public class RegistrarsUtilsContentNegotiationTest {
    private static Jooby app;
    private static int port;

    private static ContentResponse<Object> queryResponse() {
        QueryResourceInfo info = new QueryResourceInfo(null, "/fuse/query/q1", "q1", "/fuse/query/q1/cursor");
        return ContentResponse.Builder.builder(OK, NOT_FOUND).data((Object) info).requestId("req1").compose();
    }

    @BeforeClass
    public static void start() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        app = new Jooby();
        app.port(port);
        // the service's modules only activate when listed under the active profile
        app.use(ConfigFactory.empty()
                .withValue("application.profile", ConfigValueFactory.fromAnyRef("test"))
                .withValue("modules.test", ConfigValueFactory.fromIterable(
                        Collections.singletonList(LoggingJacksonModule.class.getName()))));
        new LoggingJacksonRendererRegistrar(new MetricRegistry()).register(app, null);
        // same response path as the cypher / sparql / graphql query routes
        app.get("/query", (req, rsp) -> RegistrarsUtils.with(req, rsp, queryResponse()));
        // same response style as the load routes (Results.with(ContentResponse))
        app.get("/load", req -> Results.with(queryResponse()));
        app.start("server.join=false");
        awaitServer();
    }

    private static void awaitServer() throws Exception {
        long deadline = System.currentTimeMillis() + 30_000;
        while (true) {
            try {
                if (get("/load", "application/json").status == 200) {
                    return;
                }
            } catch (IOException | AssertionError notReadyYet) {
                if (System.currentTimeMillis() > deadline) {
                    throw new IllegalStateException("test server did not start on port " + port, notReadyYet);
                }
            }
            Thread.sleep(100);
        }
    }

    @AfterClass
    public static void stop() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    public void queryWithoutAcceptHeaderReturnsJson() throws Exception {
        assertJson(get("/query", null));
    }

    @Test
    public void queryWithWildcardAcceptReturnsJson() throws Exception {
        assertJson(get("/query", "*/*"));
    }

    @Test
    public void queryWithBrowserStyleAcceptReturnsJson() throws Exception {
        assertJson(get("/query", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
    }

    @Test
    public void queryWithJsonAcceptReturnsJson() throws Exception {
        assertJson(get("/query", "application/json"));
    }

    @Test
    public void queryWithOctetStreamAcceptStillDownloads() throws Exception {
        Reply reply = get("/query", "application/octet-stream");
        Assert.assertEquals(200, reply.status);
        Assert.assertTrue(reply.contentType, reply.contentType.startsWith("application/octet-stream"));
    }

    @Test
    public void loadWithWildcardAcceptReturnsJson() throws Exception {
        assertJson(get("/load", "*/*"));
    }

    @Test
    public void loadWithoutAcceptHeaderReturnsJson() throws Exception {
        assertJson(get("/load", null));
    }

    private static void assertJson(Reply reply) throws IOException {
        Assert.assertEquals(reply.body, 200, reply.status);
        Assert.assertTrue("content type was " + reply.contentType, reply.contentType.startsWith("application/json"));
        JsonNode json = new ObjectMapper().readTree(reply.body);
        Assert.assertEquals(reply.body, "q1", json.path("data").path("resourceId").asText());
    }

    /**
     * Raw HTTP/1.1 GET, one connection per request. A plain socket is used because HttpURLConnection always adds
     * its own Accept header, so it can't express "no Accept header".
     */
    private static Reply get(String path, String accept) throws IOException {
        StringBuilder request = new StringBuilder()
                .append("GET ").append(path).append(" HTTP/1.1\r\n")
                .append("Host: localhost:").append(port).append("\r\n")
                .append("Connection: close\r\n");
        if (accept != null) {
            request.append("Accept: ").append(accept).append("\r\n");
        }
        request.append("\r\n");

        String raw;
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
            socket.setSoTimeout(10_000);
            OutputStream out = socket.getOutputStream();
            out.write(request.toString().getBytes(StandardCharsets.US_ASCII));
            out.flush();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream in = socket.getInputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                buffer.write(buf, 0, n);
            }
            raw = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }

        int headerEnd = raw.indexOf("\r\n\r\n");
        Assert.assertTrue("no HTTP response: " + raw, headerEnd > 0);
        String[] headers = raw.substring(0, headerEnd).split("\r\n");
        Reply reply = new Reply();
        reply.status = Integer.parseInt(headers[0].split(" ")[1]);
        reply.contentType = "";
        for (String header : headers) {
            if (header.toLowerCase(Locale.ROOT).startsWith("content-type:")) {
                reply.contentType = header.substring("content-type:".length()).trim();
            }
        }
        reply.body = raw.substring(headerEnd + 4);
        return reply;
    }

    private static class Reply {
        int status;
        String contentType;
        String body;
    }
}
