package com.kayhut.fuse.executor;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.driver.StandardCursorDriver;
import com.kayhut.fuse.executor.driver.StandardPageDriver;
import com.kayhut.fuse.executor.driver.StandardQueryDriver;
import com.kayhut.fuse.executor.elasticsearch.LoggingClient;
import com.kayhut.fuse.executor.mock.elasticsearch.MockClient;
import com.kayhut.fuse.executor.ontology.*;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
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
        binder.bind(CursorFactory.class).to(getCursorFactoryClass(conf)).asEagerSingleton();

        ElasticGraphConfiguration elasticGraphConfiguration = createElasticGraphConfiguration(conf);
        UniGraphConfiguration uniGraphConfiguration = createUniGraphConfiguration(conf);

        binder.bind(ElasticGraphConfiguration.class).toInstance(elasticGraphConfiguration);
        binder.bind(UniGraphConfiguration.class).toInstance(uniGraphConfiguration);

        binder.bind(Client.class).toInstance(createClient(conf, elasticGraphConfiguration));

        binder.bind(UniGraphProvider.class).to(getUniGraphProviderClass(conf)).asEagerSingleton();
        binder.bind(GraphElementSchemaProviderFactory.class).toInstance(createSchemaProviderFactory(conf));

        binder.bind(QueryDriver.class).to(StandardQueryDriver.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(StandardCursorDriver.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(StandardPageDriver.class).in(RequestScoped.class);
    }
    //endregion

    //region Private Methods
    private Client createClient(Config conf, ElasticGraphConfiguration configuration) {
        if (conf.hasPath("fuse.elasticsearch.mock")) {
            boolean clientMock = conf.getBoolean("fuse.elasticsearch.mock");
            if (clientMock) {
                System.out.println("Using mock elasticsearch client!");
                return new MockClient();
            }
        }

        Settings settings = Settings.builder().put("cluster.name", configuration.getClusterName()).build();
        TransportClient client = new PreBuiltTransportClient(settings);
        Stream.of(configuration.getClusterHosts()).forEach(host -> {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), configuration.getClusterPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return new LoggingClient(client);
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

    private Class<? extends CursorFactory> getCursorFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  CursorFactory>)Class.forName(conf.getString("fuse.cursor_factory"));
    }
    //endregion
}
