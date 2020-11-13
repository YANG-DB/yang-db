package com.yangdb.cyber.ontology.sql;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.security.SecurityAccess;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.syntax.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.ElasticsearchSQLPluginConfig;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSQLQueryAction;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.SQLService;
import com.amazon.opendistroforelasticsearch.sql.sql.config.SQLServiceConfig;
import com.amazon.opendistroforelasticsearch.sql.sql.domain.SQLQueryRequest;
import com.github.sisyphsu.dateparser.DateParser;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.elasticsearch.common.settings.Settings.EMPTY;

@Ignore("Work in progress")
public class CyberSQLQueryIT implements BaseITMarker {
    static private SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    static private DateParser parser = DateParser.newBuilder().build();
    static private RestSQLQueryAction queryAction;
    static private ClusterService service;

    @Before
    public void setup() throws Exception {
        ElasticEmbeddedNode instance = GlobalElasticEmbeddedNode.getInstance();
        //query action api
        service = instance.node.injector().getInstance(ClusterService.class);
        //cluster setting
        ClusterSettings clusterSettings = service.getClusterSettings();
//        queryAction = new RestSQLQueryAction(service, new ElasticsearchSettings(clusterSettings));

        queryAction = new RestSQLQueryAction(service, new Settings() {
            @Override
            public Object getSettingValue(Key key) {
                if (Key.QUERY_SIZE_LIMIT.equals(key)) {
                    return 200;
                }
                return EMPTY;
            }
        });
    }


    @Test
    public void testQueryTraces() throws Exception {
        SQLQueryRequest request = new SQLQueryRequest(
                new JSONObject("{\"query\": \"SELECT * from traces\"}"),
                "SELECT * from traces",
                "/fuse/query/sql",
                "");

        Client client = GlobalElasticEmbeddedNode.getInstance().node.client();
//        queryAction.prepareRequest(request, (NodeClient) client);
        query(request, (NodeClient) client);
    }

    @Test
    public void testQueryBehaviors() throws IOException, InterruptedException {

    }

    @Test
    public void testQueryTraceToBehaviors() throws IOException, InterruptedException {
    }

    public void query(SQLQueryRequest request, NodeClient nodeClient) {

        SQLService sqlService = createSQLService(service,nodeClient);
        PhysicalPlan plan;
        try {
            // For now analyzing and planning stage may throw syntax exception as well
            // which hints the fallback to legacy code is necessary here.
            plan = sqlService.plan(
                    sqlService.analyze(
                            sqlService.parse(request.getQuery())));
        } catch (SyntaxCheckException e) {
            throw new RuntimeException(e);
        }

        if (request.isExplainRequest()) {
             sqlService.explain(plan, new ResponseListener<ExecutionEngine.ExplainResponse>() {
                 @Override
                 public void onResponse(ExecutionEngine.ExplainResponse response) {
                     ExecutionEngine.ExplainResponseNode root = response.getRoot();
                 }

                 @Override
                 public void onFailure(Exception e) {
                     throw new RuntimeException(e);
                 }
             });
        }

        sqlService.execute(plan, new ResponseListener<ExecutionEngine.QueryResponse>() {
            @Override
            public void onResponse(ExecutionEngine.QueryResponse response) {
                List<ExprValue> results = response.getResults();
            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private SQLService createSQLService(ClusterService clusterService, NodeClient client) {
        return doPrivileged(() -> {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.registerBean(ClusterService.class, () -> clusterService);
            context.registerBean(NodeClient.class, () -> client);
            context.registerBean(Settings.class, () -> new Settings() {
                @Override
                public Object getSettingValue(Key key) {
                    if (Key.QUERY_SIZE_LIMIT.equals(key)) {
                        return 200;
                    }
                    if (Key.PPL_QUERY_MEMORY_LIMIT.equals(key)) {
                        return new ByteSizeValue(1, ByteSizeUnit.GB);
                    }
                    return EMPTY;
                }
            });
            context.register(ElasticsearchSQLPluginConfig.class);
            context.register(SQLServiceConfig.class);
            context.refresh();
            return context.getBean(SQLService.class);
        });
    }

    private <T> T doPrivileged(PrivilegedExceptionAction<T> action) {
        try {
            return SecurityAccess.doPrivileged(action);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to perform privileged action", e);
        }
    }

}