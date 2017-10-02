package com.kayhut.fuse.executor;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorFactory;
import com.kayhut.fuse.executor.ontology.*;
import com.kayhut.fuse.executor.ontology.promise.M1ElasticUniGraphProvider;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jooby.Env;
import org.unipop.configuration.UniGraphConfiguration;

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
        UniGraphConfiguration uniGraphConfiguration = createUniGraphConfiguration(conf);

        binder.bind(ElasticGraphConfiguration.class).toInstance(elasticGraphConfiguration);
        binder.bind(UniGraphConfiguration.class).toInstance(uniGraphConfiguration);

        binder.bind(Client.class).toInstance(createClient(elasticGraphConfiguration));

        binder.bind(UniGraphProvider.class).to(getUniGraphProviderClass(conf)).asEagerSingleton();
        binder.bind(GraphElementSchemaProviderFactory.class).toInstance(createSchemaProviderFactory(conf));
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

    private UniGraphConfiguration createUniGraphConfiguration(Config conf) {
        UniGraphConfiguration configuration = new UniGraphConfiguration();
        configuration.setBulkMax(conf.getInt("unipop.bulk.max"));
        configuration.setBulkStart(conf.getInt("unipop.bulk.start"));
        configuration.setBulkMultiplier(conf.getInt("unipop.bulk.multiplier"));
        return configuration;
    }

    private GraphElementSchemaProviderFactory createSchemaProviderFactory(Config conf) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        GraphElementSchemaProviderFactory physicalSchemaProviderFactory =
                (GraphElementSchemaProviderFactory)(Class.forName(
                        conf.getString("fuse.physical_schema_provider_factory_class")).newInstance());

        return new OntologyGraphElementSchemaProviderFactory(physicalSchemaProviderFactory);
    }

    private Class<? extends UniGraphProvider> getUniGraphProviderClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  UniGraphProvider>)Class.forName(conf.getString("fuse.unigraph_provider"));
    }
    //endregion
}
