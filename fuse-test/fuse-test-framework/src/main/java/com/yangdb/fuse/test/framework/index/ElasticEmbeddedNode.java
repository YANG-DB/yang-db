package com.yangdb.fuse.test.framework.index;

/*-
 * #%L
 * fuse-test-framework
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

import com.yangdb.fuse.opensearch.client.ClientFactory;
import org.opensearch.client.Client;
import org.opensearch.client.core.MainResponse;
import org.opensearch.common.settings.Settings;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.plugins.Plugin;
import org.opensearch.transport.Netty4Plugin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.yangdb.fuse.test.framework.TestUtil.deleteFolder;


public class ElasticEmbeddedNode implements AutoCloseable {
    public static final String FUSE_TEST_ELASTIC = "fuse.test_elastic";
    /** Override with -Dfuse.test.http.port / -Dfuse.test.transport.port to run several test JVMs side by side. */
    public static final int HTTP_PORT = Integer.getInteger("fuse.test.http.port", 9200);
    public static final int TRANSPORT_PORT = Integer.getInteger("fuse.test.transport.port", 9300);
    /** Override with -Dfuse.test.data.dir, e.g. to keep node data off filesystems where index deletion can't complete. */
    public static final String DATA_DIR = System.getProperty("fuse.test.data.dir", "target/es");

    static {
        //see https://github.com/testcontainers/testcontainers-java/issues/1009 issue with netty & E/S
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins, Path path, String nodeName) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, new HashMap<>(), path, () -> nodeName), classpathPlugins, false);
        }

/*
        @Override
        protected void registerDerivedNodeNameWithLogger(String nodeName) {
            LogConfigurator.loadLog4jPlugins();
            LogConfigurator.setNodeName(nodeName);
        }
*/
    }
    //endregion

    //region Members
    private static String esWorkingDir;
    private static int numberOfShards;
    public static Node node;

    static String nodeName = FUSE_TEST_ELASTIC;

    static int httpPort = HTTP_PORT;
    static int httpTransportPort = TRANSPORT_PORT;

    static Client client = null;
    // true when the client is a REST bridge client to an externally running cluster (not owned by an embedded node)
    private static boolean remoteClient = false;
    //endregion

    //region Constructors
    ElasticEmbeddedNode(String clusterName) throws Exception {
        this(Settings.EMPTY, DATA_DIR, HTTP_PORT, TRANSPORT_PORT, clusterName);
    }

    ElasticEmbeddedNode(Settings settings, String clusterName, int numberOfShards) throws Exception {
        this(settings, DATA_DIR, HTTP_PORT, TRANSPORT_PORT, clusterName, numberOfShards);
    }

    ElasticEmbeddedNode() throws Exception {
        this(Settings.EMPTY, DATA_DIR, HTTP_PORT, TRANSPORT_PORT, "fuse.test_elastic");
    }

    ElasticEmbeddedNode(Settings settings, ElasticIndexConfigurer... configurers) throws Exception {
        this(settings, DATA_DIR, HTTP_PORT, TRANSPORT_PORT, "fuse.test_elastic", configurers);
    }

    ElasticEmbeddedNode(Settings settings, String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, ElasticIndexConfigurer... configurers) throws Exception {
        this(settings, esWorkingDir, httpPort, httpTransportPort, nodeName, 1, configurers);
    }

    ElasticEmbeddedNode(Settings settings) throws Exception {
        this(settings, esWorkingDir, httpPort, httpTransportPort, nodeName, 1);
    }

    ElasticEmbeddedNode(Settings additional, String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, ElasticIndexConfigurer... configurers) throws Exception {
        ElasticEmbeddedNode.httpTransportPort = httpTransportPort;
        ElasticEmbeddedNode.nodeName = nodeName;
        ElasticEmbeddedNode.esWorkingDir = esWorkingDir;
        ElasticEmbeddedNode.httpPort = httpPort;
        ElasticEmbeddedNode.numberOfShards = numberOfShards;
        prepare(additional);

        for (ElasticIndexConfigurer configurer : configurers) {
            configurer.configure(getClient(nodeName, httpTransportPort));
        }
    }

    //endregion

    //region Methods
    public static Client getClient() {
        return getClient(nodeName, httpTransportPort);
    }

    //region Methods

    public static Client getClient(MainResponse info) {
        return getClient(info.getClusterName(), httpTransportPort);
    }

    public static Client getClient(String nodeName) {
        return getClient(nodeName, httpTransportPort);
    }

    /**
     * Client of the embedded node when one is running in this JVM; otherwise a REST bridge client
     * (fuse-opensearch-client) to an already running cluster named {@code clusterName} on localhost. The bridge talks
     * HTTP: for the configured transport port it uses the configured HTTP port ({@code -Dfuse.test.http.port}), for
     * any other transport port it uses {@code transportPort - 100}.
     */
    public static Client getClient(String clusterName, int transportPort) {
        if (client == null) {
            System.out.println("Setting client " + clusterName);
            if (node != null) {
                client = node.client();
            } else {
                client = ClientFactory.remote(new String[]{InetAddress.getLoopbackAddress().getHostAddress()},
                        remoteHttpPort(transportPort),
                        Settings.builder().put("cluster.name", clusterName).build());
                remoteClient = true;
            }
        }

        return client;
    }

    static int remoteHttpPort(int transportPort) {
        return transportPort == httpTransportPort ? httpPort : ClientFactory.httpPortFromTransportPort(transportPort);
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing E/S embedded");
        closeClient();
        if (this.node != null) {
            this.node.close();
            this.node = null;
        }


        deleteFolder(esWorkingDir);
    }

    public static void closeClient() {
        // an embedded node's client is closed together with the node; only a remote bridge client is ours to close
        if (client != null && remoteClient) {
            client.close();
        }
        client = null;
        remoteClient = false;
    }

    private void prepare(Settings additional) throws Exception {
        this.close();

        Settings settings = getBuilder(additional);

        System.out.println("Setting E/S embedded "+nodeName);
        Collection<Class<? extends Plugin>> plugins = new ArrayList<>();
        plugins.add(Netty4Plugin.class);
        this.node = new PluginConfigurableNode(settings, plugins, Paths.get(esWorkingDir), nodeName);
        this.node = this.node.start();
        System.out.println("Started E/S Embedded");

    }

    public static Settings getBuilder(Settings additional) {
        return Settings.builder()
                .put("cluster.name", nodeName)
                .put("node.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.cors.enabled", "true")
                // a nearly full developer disk must not turn test indices read-only
                .put("cluster.routing.allocation.disk.threshold_enabled", false)
//                .put("script.auto_reload_enabled", "false")
                .put("transport.port", httpTransportPort)
                .put(additional)
                .build();
    }
    //endregion

    public static boolean isAvailable(int portNr) {
        boolean portFree;
        try (ServerSocket ignored = new ServerSocket(portNr)) {
            portFree = true;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }
}
