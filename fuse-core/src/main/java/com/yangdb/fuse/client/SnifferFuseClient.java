package com.yangdb.fuse.client;

/*-
 * #%L
 * fuse-core
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



import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.*;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.PlanTraceOptions;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.typesafe.config.ConfigFactory.defaultApplication;

/**
 * Fuse Cluster aware client which samples the elasticsearch 'fuse_node_info' index which holds the cluster
 * status for all fuse nodes
 *
 * This client expects application.conf file in the classpath containing the next configurations:
 * 
 * fuse.port = 8888
 * fuse.protocol = http
 * fuse.base.uri = /fuse
 *
 * elasticsearch.hosts = [ "127.0.0.1" ]
 * elasticsearch.port = 9300
 * elasticsearch.cluster_name = clusterName
 */
public class SnifferFuseClient implements FuseClient{
    public static final String SYSTEM = "fuse_node_info";
    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String UPDATE_TIME = "updateTime";
    public static final String RESOURCE = "resource";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Client client;
    private LoadingCache<String, FuseClient>  fuseClients;
    private Map<String,Map> nodeStats;
    private ElasticGraphConfiguration configuration;

    private final int fusePort;
    private final String fuseProtocol;
    private final String fuseBaseUri;

    public SnifferFuseClient() {

        final Config conf = defaultApplication();
        fusePort = conf.getInt("fuse.port");
        fuseProtocol = conf.getString("fuse.protocol");
        fuseBaseUri = conf.getString("fuse.base.uri");

        nodeStats = new HashMap<>();
        fuseClients = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(key -> this);

        this.configuration = createElasticGraphConfiguration(conf);
        this.client = getClient(this.configuration);
        //first time init the cluster map state
        loadClusterState();

        //sniff fuse cluster state from elastic fuse_node_info index
        scheduler.scheduleAtFixedRate(() -> loadClusterState(), 5,10,TimeUnit.SECONDS);
    }

    private void loadClusterState() {
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        final SearchResponse response = client.prepareSearch()
                .setIndices(SYSTEM)
                .setTypes(RESOURCE)
                .setQuery(queryBuilder)
                .execute().actionGet();

        final SearchHits hits = response.getHits();
        hits.iterator().forEachRemaining(h->updateNodeStatus(h.getId(),h.getSourceAsMap()));
    }

    private void updateNodeStatus(String id, Map<String, Object> source) {
        try {
            this.nodeStats.put(id,source);
            this.fuseClients.put(id,new BaseFuseClient(new URL(fuseProtocol,id,fusePort,fuseBaseUri).toString()));
            //todo remove non reporting nodes from client pool
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FuseClient selectNode() {
        //todo select best appropriate node according to least busy (with respect to living fuse nodes)
        return fuseClients.asMap().values().iterator().next();
    }

    private FuseClient selectNode(String url) {
        //todo select best appropriate node according to least busy & relevant to given url (with respect to living fuse nodes)
        return fuseClients.asMap().values().iterator().next();
    }

    @Override
    public FuseResourceInfo getFuseInfo() throws IOException {
        return selectNode().getFuseInfo();
    }

    @Override
    public Object getId(String name, int numIds) throws IOException {
        return selectNode().getId(name,numIds);
    }

    @Override
    public ResultResourceInfo upsertCsvData(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException {
        return selectNode().upsertCsvData(ontology,type,label,resource);
    }

    @Override
    public ResultResourceInfo loadCsvData(String ontology, String type, String label, String model) throws IOException {
        return selectNode().loadCsvData(ontology,type,label,model);
    }

    @Override
    public ResultResourceInfo loadCsvData(String ontology, String type, String label, URL resource) throws IOException {
        return selectNode().loadCsvData(ontology,type,label,resource);

    }

    @Override
    public ResultResourceInfo uploadCsvFile(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException {
        return selectNode().uploadCsvFile(ontology ,type,label, resource);
    }

    @Override
    public ResultResourceInfo upsertCsvFile(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException {
        return selectNode().upsertCsvFile(ontology,type,label,resource);
    }

    @Override
    public ResultResourceInfo upsertGraphData(String ontology, URL resource) throws IOException {
        return selectNode().upsertGraphData(ontology,resource);
    }

    @Override
    public ResultResourceInfo loadGraphData(String ontology, LogicalGraphModel model) throws IOException {
        return selectNode().loadGraphData(ontology,model);
    }

    @Override
    public ResultResourceInfo loadGraphData(String ontology, URL resource) throws IOException {
        return selectNode().loadGraphData(ontology,resource);
    }

    @Override
    public ResultResourceInfo uploadGraphFile(String ontology, URL resource) throws IOException, URISyntaxException {
        return selectNode().uploadGraphFile(ontology,resource);
    }

    @Override
    public ResultResourceInfo upsertGraphFile(String ontology, URL resource) throws IOException, URISyntaxException {
        return selectNode().upsertGraphFile(ontology,resource);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException {
        return selectNode().postQuery(queryStoreUrl,query);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, String query, String ontology) throws IOException {
        return selectNode().postQuery(queryStoreUrl,query,ontology);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, CreateQueryRequest request) throws IOException {
        return selectNode().postQuery(queryStoreUrl,request);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException {
        return selectNode().postQuery(queryStoreUrl,query,planTraceOptions);
    }

    @Override
    public QueryResourceInfo postGraphQLQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException {
        return selectNode().postGraphQLQuery(queryStoreUrl,query,ontology,planTraceOptions);
    }

    @Override
    public QueryResourceInfo postCypherQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException {
        return selectNode().postCypherQuery(queryStoreUrl,query,ontology,planTraceOptions);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException {
        return selectNode().postQuery(queryStoreUrl,query,id,name);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name, CreateCursorRequest createCursorRequest) throws IOException {
        return selectNode().postQuery(queryStoreUrl,query,id,name,createCursorRequest);
    }

    @Override
    public String initIndices(String ontology) {
        return selectNode().initIndices(ontology);
    }

    @Override
    public String dropIndices(String ontology) {
        return selectNode().dropIndices(ontology);
    }

    @Override
    public CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        return selectNode(cursorStoreUrl).postCursor(cursorStoreUrl);
    }

    @Override
    public CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest cursorRequest) throws IOException {
        return selectNode(cursorStoreUrl).postCursor(cursorStoreUrl,cursorRequest);
    }

    @Override
    public PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException {
        return selectNode(pageStoreUrl).postPage(pageStoreUrl,pageSize);
    }

    @Override
    public PageResourceInfo getPage(String pageUrl, String pageId) throws IOException {
        return selectNode(pageUrl).getPage(pageUrl,pageId);
    }

    @Override
    public PageResourceInfo getPage(String pageUrl) throws IOException {
        return selectNode(pageUrl).getPage(pageUrl);
    }

    @Override
    public QueryResourceInfo getQuery(String queryUrl, String queryId) throws IOException {
        return selectNode(queryUrl).getQuery(queryUrl,queryId);
    }

    @Override
    public CursorResourceInfo getCursor(String cursorUrl, String cursorId) throws IOException {
        return selectNode(cursorUrl).getCursor(cursorUrl,cursorId);
    }

    @Override
    public Ontology getOntology(String ontologyUrl) throws IOException {
        return selectNode().getOntology(ontologyUrl);
    }

    @Override
    public Query getQuery(String queryUrl,Class<? extends Query> klass) throws IOException {
        return selectNode(queryUrl).getQuery(queryUrl,klass);
    }

    @Override
    public QueryResultBase getPageData(String pageDataUrl, TypeReference typeReference) throws IOException {
        return selectNode(pageDataUrl).getPageData(pageDataUrl,typeReference);
    }

    @Override
    public QueryResultBase getPageData(String pageDataUrl) throws IOException {
        return selectNode(pageDataUrl).getPageData(pageDataUrl);
    }

    @Override
    public String getPageDataPlain(String pageDataUrl) throws IOException {
        return selectNode(pageDataUrl).getPageDataPlain(pageDataUrl);
    }

    @Override
    public String getPlan(String planUrl) throws IOException {
        return selectNode(planUrl).getPlan(planUrl);
    }

    @Override
    public Plan getPlanObject(String planUrl) throws IOException {
        return selectNode(planUrl).getPlanObject(planUrl);
    }

    @Override
    public Long getFuseSnowflakeId() throws IOException {
        return selectNode().getFuseSnowflakeId();
    }

    @Override
    public String getFuseUrl() {
        return selectNode().getFuseUrl();
    }


    @Override
    public String deleteQuery(QueryResourceInfo queryResourceInfo) {
        return null;
    }

    @Override
    public boolean shutdown() {
        client.close();
        scheduler.shutdownNow();
        return true;
    }

    private List<String> getStringList(Config conf, String key) {
        try {
            return conf.getStringList(key);
        } catch (Exception ex) {
            String strList = conf.getString(key);
            return Stream.of(strList.split(",")).toJavaList();
        }
    }

    private ElasticGraphConfiguration createElasticGraphConfiguration(Config conf) {
        ElasticGraphConfiguration configuration = new ElasticGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(getStringList(conf, "elasticsearch.hosts")).toJavaArray(String.class));
        configuration.setClusterPort(conf.getInt("elasticsearch.port"));
        configuration.setClusterName(conf.getString("elasticsearch.cluster_name"));

        configuration.setClientTransportIgnoreClusterName(conf.hasPath("client.transport.ignore_cluster_name") &&
                conf.getBoolean("client.transport.ignore_cluster_name"));

        return configuration;
    }

    private static Client getClient(ElasticGraphConfiguration configuration) {
        Settings settings = Settings.builder()
                .put("cluster.name", configuration.getClusterName())
                .put("client.transport.ignore_cluster_name", configuration.isClientTransportIgnoreClusterName())
                .build();
        TransportClient client = new PreBuiltTransportClient(settings);
        configuration.getClusterHosts().forEach(host -> {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(String.valueOf(host)), configuration.getClusterPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
        return client;
    }

    private class ElasticGraphConfiguration {
        private List<String> clusterHosts;
        private int clusterPort;
        private String clusterName;
        private boolean clientTransportIgnoreClusterName;

        public List<String> getClusterHosts() {
            return clusterHosts;
        }

        public void setClusterHosts(String ... clusterHosts) {
            this.clusterHosts = Arrays.asList(clusterHosts);
        }

        public int getClusterPort() {
            return clusterPort;
        }

        public void setClusterPort(int clusterPort) {
            this.clusterPort = clusterPort;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public boolean isClientTransportIgnoreClusterName() {
            return clientTransportIgnoreClusterName;
        }

        public void setClientTransportIgnoreClusterName(boolean clientTransportIgnoreClusterName) {
            this.clientTransportIgnoreClusterName = clientTransportIgnoreClusterName;
        }
    }
}
