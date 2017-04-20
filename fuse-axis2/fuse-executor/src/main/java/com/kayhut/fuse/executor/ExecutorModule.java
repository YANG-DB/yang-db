package com.kayhut.fuse.executor;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorFactory;
import com.kayhut.fuse.executor.uniGraphProvider.ElasticUniGraphProvider;
import com.kayhut.fuse.executor.uniGraphProvider.UniGraphProvider;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.SimplePhysicalIndexProvider;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jooby.Env;
import org.jooby.Jooby;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by lior on 22/02/2017.
 */
public class ExecutorModule extends ModuleBase {
    //region Jooby.Module Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(CursorFactory.class).to(TraversalCursorFactory.class).asEagerSingleton();

        ElasticGraphConfiguration elasticGraphConfiguration = createElasticGraphConfiguration(conf);
        binder.bind(ElasticGraphConfiguration.class).toInstance(elasticGraphConfiguration);
        binder.bind(Client.class).toInstance(createClient(elasticGraphConfiguration));

        binder.bind(UniGraphProvider.class).to(ElasticUniGraphProvider.class).asEagerSingleton();

        binder.bind(PhysicalIndexProvider.class).toInstance(createPhysicalIndex(conf));
    }
    //endregion

    //region Private Methods
    private Client createClient(ElasticGraphConfiguration configuration) {
        Settings settings = Settings.settingsBuilder().put("cluster.name", configuration.getClusterName()).build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        Stream.of(configuration.getClusterHosts()).forEach(host -> {
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), configuration.getClusterPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return client;
    }

    private ElasticGraphConfiguration createElasticGraphConfiguration(Config conf) {
        ElasticGraphConfiguration configuration = new ElasticGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(conf.getStringList("elasticsearch.hosts")).toJavaArray(String.class));
        configuration.setClusterPort(conf.getInt("elasticsearch.port"));
        configuration.setClusterName(conf.getString("elasticsearch.cluster_name"));
        configuration.setElasticGraphDefaultSearchSize(conf.getLong("elasticsearch.default_search_size"));
        configuration.setElasticGraphMaxSearchSize(conf.getLong("elasticsearch.max_search_size"));
        configuration.setElasticGraphScrollSize(conf.getInt("elasticsearch.scroll_size"));
        configuration.setElasticGraphScrollTime(conf.getInt("elasticsearch.scroll_time"));
        return configuration;
    }

    private PhysicalIndexProvider createPhysicalIndex(Config conf) {
        return new SimplePhysicalIndexProvider(conf.getString("fuse.vertex_index_name"), conf.getString("fuse.edge_index_name"));
    }
    //endregion
}
