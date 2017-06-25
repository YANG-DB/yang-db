package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javaslang.collection.Stream;
import org.jooby.Jooby;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Created by Roman on 05/06/2017.
 */
public class FuseRunner {
    public static void main(final String[] args) {
        System.out.println("Args:");
        Stream.of(args).forEach(System.out::println);

        final String applicationConfFilename = args.length > 0 ?
                args[0] : "application.conf";

        final String activeProfile = args.length > 1 ?
                args[1] : "activeProfile";

        final String logbackConfigurationFilename = args.length > 2 ?
                args[2] : "logback.xml";

        //region embedded profiler (nudge4j)
        ((Consumer<Object[]>) args1 -> {
            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
                engine.put("args", args1);
                String p = "com.sun.net.httpserver.";
                Class<?>
                        HH = Class.forName(p + "HttpHandler"),
                        HE = Class.forName(p + "HttpExchange"),
                        HD = Class.forName(p + "Headers"),
                        HS = Class.forName(p + "HttpServer");
                Method
                        m0 = HE.getMethod("getRequestURI"),
                        m1 = HE.getMethod("getResponseHeaders"),
                        m2 = HE.getMethod("sendResponseHeaders", int.class, long.class),
                        m3 = HE.getMethod("getResponseBody"),
                        m4 = HS.getMethod("create", InetSocketAddress.class, int.class),
                        m5 = HS.getMethod("createContext", String.class, HH),
                        m6 = HS.getMethod("setExecutor", Executor.class),
                        m7 = HS.getMethod("start"),
                        m8 = HD.getMethod("set", String.class, String.class),
                        m9 = HE.getMethod("getRequestBody"),
                        mA = HE.getMethod("getRequestHeaders");
                Object server = m4.invoke(null, new InetSocketAddress(
                        InetAddress.getLoopbackAddress(), (int) args1[0]), 0);
                m5.invoke(server, "/", Proxy.newProxyInstance(
                        HH.getClassLoader(),
                        new Class[]{HH},
                        new InvocationHandler() {
                            Charset UTF8 = StandardCharsets.UTF_8;
                            byte data[] = new byte[10000];

                            void send(Object httpExchange, byte array[], int max, String contentType) throws Exception {
                                m8.invoke(m1.invoke(httpExchange), "Content-Type", contentType);
                                m2.invoke(httpExchange, 200, max);
                                try (OutputStream os = (OutputStream) m3.invoke(httpExchange)) {
                                    os.write(array, 0, max);
                                }
                            }

                            @SuppressWarnings("rawtypes")
                            public synchronized Object invoke(Object pxy, Method m, Object[] params) throws Exception {
                                Object httpExchange = params[0];
                                String uri = m0.invoke(httpExchange).toString();
                                if (uri.startsWith("/js")) {
                                    Map requestHeaders = (Map) mA.invoke(httpExchange);
                                    if (requestHeaders.containsKey("Origin")) {
                                        String origin = "" + ((List) requestHeaders.get("Origin")).get(0);
                                        if (origin.equals("http://localhost:" + args1[0]) == false &&
                                                origin.equals("http://127.0.0.1:" + args1[0]) == false) return null;
                                    } else if (requestHeaders.containsKey("Referer")) {
                                        String referer = "" + ((List) requestHeaders.get("Referer")).get(0);
                                        if (referer.startsWith("http://localhost:" + args1[0]) == false &&
                                                referer.startsWith("http://127.0.0.1:" + args1[0]) == false)
                                            return null;
                                    } else return null;

                                    byte array[];
                                    try (Reader r = new InputStreamReader((InputStream) m9.invoke(httpExchange), UTF8)) {
                                        array = ("" + engine.eval(r)).getBytes(UTF8);
                                    } catch (Exception e) {
                                        OutputStream os = new ByteArrayOutputStream();
                                        e.printStackTrace(new PrintStream(os));
                                        array = ("err::" + os).getBytes(UTF8);
                                    }
                                    send(httpExchange, array, array.length, "text/plain");
                                    return null;
                                }
                                if ("/".equals(uri)) uri = "/index.html";
                                String url = "https://lorenzoongithub.github.io/nudge4j/localhost.port" + uri;
                                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                                c.setRequestMethod("GET");
                                int responseCode = c.getResponseCode();
                                if (responseCode != 200) {
                                    m2.invoke(httpExchange, responseCode, -1);
                                    return null;
                                }
                                int count = 0;
                                try (InputStream is = c.getInputStream()) {
                                    for (int b = is.read(); b != -1; b = is.read()) data[count++] = (byte) b;
                                }
                                send(httpExchange, data, count, (
                                        (uri.endsWith(".ico")) ? "image/x-icon" :
                                                (uri.endsWith(".css")) ? "text/css" :
                                                        (uri.endsWith(".png")) ? "image/png" :
                                                                (uri.endsWith(".js")) ? "application/javascript" :
                                                                        "text/html"));
                                return null;
                            }
                        }
                ));
                m6.invoke(server, new Object[]{null});
                m7.invoke(server);
                System.out.println("nudge4j serving on port:" + args1[0]);
            } catch (Exception e) {
                throw new InternalError(e);
            }
        }).accept(new Object[]{5050});
// endregion *nudge4j

        new FuseRunner().run(new Options(applicationConfFilename, activeProfile, logbackConfigurationFilename, true));
    }

    public void run() {
        this.run(null, new Options());
    }

    public void run(Jooby app) {
        this.run(app, new Options());
    }

    public void run(Options options) {
        this.run(null, options);
    }

    public void run(Jooby app, Options options) {
        String[] joobyArgs = new String[]{
                "logback.configurationFile=" + options.getLogbackConfigrationFilename(),
                "server.join=" + (options.isServerJoin() ? "true" : "false")
        };

        Jooby.run(() -> app != null ?
                        app :
                        new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                                .conf(new File(options.getApplicationConfFilename()), options.getActiveProfile()),
                joobyArgs);
    }

    public static class Options {
        //region Constructors
        public Options() {
            this("application.conf", "activeProfile", "logback.xml", true);
        }

        public Options(String logbackConfigrationFilename, boolean serverJoin) {
            this(null, null, logbackConfigrationFilename, serverJoin);
        }

        public Options(String applicationConfFilename, String activeProfile, String logbackConfigrationFilename, boolean serverJoin) {
            this.applicationConfFilename = applicationConfFilename;
            this.activeProfile = activeProfile;
            this.logbackConfigrationFilename = logbackConfigrationFilename;
            this.serverJoin = serverJoin;
        }
        //endregion

        //region Properties
        public String getApplicationConfFilename() {
            return applicationConfFilename;
        }

        public String getActiveProfile() {
            return activeProfile;
        }

        public String getLogbackConfigrationFilename() {
            return logbackConfigrationFilename;
        }

        public boolean isServerJoin() {
            return serverJoin;
        }
        //endregion

        //region Fields
        private String applicationConfFilename;
        private String activeProfile;
        private String logbackConfigrationFilename;
        private boolean serverJoin;
        //endregion
    }
}
