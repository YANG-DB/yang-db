package com.yangdb.fuse.services.embedded;

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

import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;

import org.opensearch.client.transport.TransportClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.painless.PainlessPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.transport.Netty4Plugin;
import org.opensearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Created by moti on 3/19/2017.
 */
public class ElasticEmbeddedNode implements AutoCloseable {

    public static final String TARGET_ES = "target/es";
    public static final int HTTP_PORT = 9200;
    public static final String FUSE_TEST_ELASTIC = "fuse.test_elastic";

    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins, Path path, String nodeName) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, new HashMap<>(), path, () -> nodeName), classpathPlugins, false);
        }

    }
    //endregion

    //region Members
    private final boolean deleteOnLoad;
    private final int httpPort;
    private final String esWorkingDir;
    private final int numberOfShards;
    private Node node;

    static int httpTransportPort;
    static String nodeName;
    static TransportClient client = null;
    //endregion

    //region Constructors

    public ElasticEmbeddedNode(OpensearchGraphConfiguration configuration) throws Exception {
        this(TARGET_ES, HTTP_PORT, configuration.getClusterPort(), configuration.getClusterName(), 1, false, configuration.getClusterProps());
    }

    public ElasticEmbeddedNode(String clusterName) throws Exception {
        this(TARGET_ES, HTTP_PORT, 9300, clusterName, true);
    }

    public ElasticEmbeddedNode(String clusterName, int numberOfShards) throws Exception {
        this(TARGET_ES, HTTP_PORT, 9300, clusterName, numberOfShards, false, Collections.emptyMap());
    }

    public ElasticEmbeddedNode() throws Exception {
        this(TARGET_ES, HTTP_PORT, 9300, FUSE_TEST_ELASTIC, true);
    }

    public ElasticEmbeddedNode(ElasticIndexConfigurer... configurers) throws Exception {
        this(TARGET_ES, HTTP_PORT, 9300, FUSE_TEST_ELASTIC, true, configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, boolean deleteOnLoad, ElasticIndexConfigurer... configurers) throws Exception {
        this(esWorkingDir, httpPort, httpTransportPort, nodeName, 1, deleteOnLoad, Collections.emptyMap(), configurers);
    }


    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, boolean deleteOnLoad, Map<String, String> values, ElasticIndexConfigurer... configurers) throws Exception {
        ElasticEmbeddedNode.httpTransportPort = httpTransportPort;
        ElasticEmbeddedNode.nodeName = nodeName;
        this.deleteOnLoad = deleteOnLoad;
        this.esWorkingDir = esWorkingDir;
        this.httpPort = httpPort;
        this.numberOfShards = numberOfShards;
        try {
            prepare(values);
        } catch (Throwable t) {
            throw new UnknownError(t.getMessage());
        }
        for (ElasticIndexConfigurer configurer : configurers) {
            configurer.configure(getClient(nodeName, httpTransportPort));
        }
    }

    //endregion

    //region Methods
    public static TransportClient getClient() {
        return getClient(nodeName, httpTransportPort);
    }

    public static TransportClient getClient(String nodeName, int httpTransportPort) {
        if (client == null) {
            try {
                Settings settings = Settings.builder()
                        .put("cluster.name", nodeName)
                        .build();
                client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            } catch (UnknownHostException e) {
                throw new UnknownError(e.getMessage());
            }
        }

        return client;
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing");
        closeClient();
        if (this.node != null) {
            this.node.close();
            this.node = null;
        }


        if (deleteOnLoad) {
            deleteFolder(esWorkingDir);
        }
    }

    public static void closeClient() {
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void prepare(Map<String, String> values) throws Exception {
        if (deleteOnLoad) {
            this.close();
        }

        Settings.Builder builder = Settings.builder()
                .put("cluster.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.cors.enabled", "true")
//                .put("script.auto_reload_enabled", "false")
                .put("transport.tcp.port", httpTransportPort);
        //populate additional cluster config params
        values.forEach(builder::put);
        //build setting
        Settings settings = builder.build();
        this.node = new PluginConfigurableNode(settings, Arrays.asList(
                Netty4Plugin.class,
                PainlessPlugin.class
        ), Paths.get(esWorkingDir), nodeName);

        this.node = this.node.start();
        System.out.println("Node started successfully on " + esWorkingDir);
    }

    private static void deleteFolder(String folder) {
        File folderFile = new File(folder);
        File[] files = folderFile.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        folderFile.delete();
    }

    //endregion
}
