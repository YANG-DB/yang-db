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

import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.LogConfigurator;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import static com.yangdb.fuse.test.framework.TestUtil.deleteFolder;


/**
 * Created by moti on 3/19/2017.
 */
public class ElasticEmbeddedNode implements AutoCloseable {
    public static final String FUSE_TEST_ELASTIC = "fuse.test_elastic";

    static {
        //see https://github.com/testcontainers/testcontainers-java/issues/1009 issue with netty & E/S
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins,false);
        }

        @Override
        protected void registerDerivedNodeNameWithLogger(String nodeName) {
            LogConfigurator.loadLog4jPlugins();
            LogConfigurator.setNodeName(nodeName);
        }
    }
    //endregion

    //region Members
    private static String esWorkingDir;
    private static int numberOfShards;
    private Node node;

    static String nodeName = FUSE_TEST_ELASTIC;

    static int httpPort = 9200;
    static int httpTransportPort = 9300;

    static TransportClient client = null;
    //endregion

    //region Constructors
    public ElasticEmbeddedNode(String clusterName) throws Exception {
        this("target/es", 9200, 9300, clusterName);
    }

    public ElasticEmbeddedNode(String clusterName, int numberOfShards) throws Exception {
        this("target/es", 9200, 9300, clusterName, numberOfShards);
    }

    public ElasticEmbeddedNode() throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic");
    }

    public ElasticEmbeddedNode(ElasticIndexConfigurer... configurers) throws Exception {
        this("target/es", 9200, 9300, "fuse.test_elastic", configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, ElasticIndexConfigurer... configurers) throws Exception {
        this(esWorkingDir, httpPort, httpTransportPort, nodeName, 1, configurers);
    }

    public ElasticEmbeddedNode(String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, ElasticIndexConfigurer... configurers) throws Exception {
        ElasticEmbeddedNode.httpTransportPort = httpTransportPort;
        ElasticEmbeddedNode.nodeName = nodeName;
        ElasticEmbeddedNode.esWorkingDir = esWorkingDir;
        ElasticEmbeddedNode.httpPort = httpPort;
        ElasticEmbeddedNode.numberOfShards = numberOfShards;
        prepare();

        for (ElasticIndexConfigurer configurer : configurers) {
            configurer.configure(getClient(nodeName,httpTransportPort));
        }
    }

    //endregion

    //region Methods
    public static TransportClient getClient() {
        return getClient(nodeName,httpTransportPort);
    }

    //region Methods
    public static TransportClient getClient(String nodeName) {
        return getClient(nodeName,httpTransportPort);
    }

    public static TransportClient getClient(String nodeName,int httpTransportPort) {
        if (client == null) {
            try {
                System.out.println("Setting client "+nodeName);
                Settings settings = Settings.builder()
                        .put("cluster.name", nodeName)
                        .put("node.name", nodeName)
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
        System.out.println("Closing E/S embedded");
        closeClient();
        if (this.node != null) {
            this.node.close();
            this.node = null;
        }


        deleteFolder(esWorkingDir);
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

    private void prepare() throws Exception {
        this.close();

        Settings settings = Settings.builder()
                .put("cluster.name", nodeName)
                .put("node.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.enabled", "true")
//                .put("script.auto_reload_enabled", "false")
                .put("transport.tcp.port", httpTransportPort)
                .build();

        System.out.println("Setting E/S embedded "+nodeName);
        this.node = new PluginConfigurableNode(settings, Arrays.asList(
                Netty4Plugin.class,
//                CommonScriptPlugin.class,
                CommonAnalysisPlugin.class
        ));

        this.node = this.node.start();
        System.out.println("Started E/S Embedded");

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
