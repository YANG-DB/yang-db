package com.kayhut.fuse.executor;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.driver.StandardCursorDriver;
import com.kayhut.fuse.executor.driver.StandardPageDriver;
import com.kayhut.fuse.executor.driver.StandardQueryDriver;
import com.kayhut.fuse.executor.elasticsearch.ClientProvider;
import com.kayhut.fuse.executor.logging.LoggingCursorFactory;
import com.kayhut.fuse.executor.ontology.*;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.elasticsearch.client.Client;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.configuration.UniGraphConfiguration;

import static com.google.inject.name.Names.named;

/**
 * Created by lior on 22/02/2017.
 */
public class ExecutorModule extends ModuleBase {
    //region Jooby.Module Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {

        bindCursorFactory(env, conf, binder);

        ElasticGraphConfiguration elasticGraphConfiguration = createElasticGraphConfiguration(conf);
        UniGraphConfiguration uniGraphConfiguration = createUniGraphConfiguration(conf);

        binder.bind(ElasticGraphConfiguration.class).toInstance(elasticGraphConfiguration);
        binder.bind(UniGraphConfiguration.class).toInstance(uniGraphConfiguration);

        binder.bind(Client.class).toProvider(ClientProvider.class).asEagerSingleton();

        binder.bind(UniGraphProvider.class).to(getUniGraphProviderClass(conf)).asEagerSingleton();
        binder.bind(GraphElementSchemaProviderFactory.class).toInstance(createSchemaProviderFactory(conf));

        binder.bind(QueryDriver.class).to(StandardQueryDriver.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(StandardCursorDriver.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(StandardPageDriver.class).in(RequestScoped.class);
    }
    //endregion

    //region Private Methods
    private void bindCursorFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(CursorFactory.class)
                            .annotatedWith(named(LoggingCursorFactory.cursorFactoryParameter))
                            .to(getCursorFactoryClass(conf))
                            .asEagerSingleton();
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingCursorFactory.cursorLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(Cursor.class));
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingCursorFactory.traversalLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(Traversal.class));
                    this.bind(CursorFactory.class)
                            .to(LoggingCursorFactory.class)
                            .asEagerSingleton();

                    this.expose(CursorFactory.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
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
