package com.yangdb.fuse.executor;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.internal.SingletonScope;
import com.google.inject.name.Names;
import com.yangdb.fuse.core.driver.StandardCursorDriver;
import com.yangdb.fuse.core.driver.StandardPageDriver;
import com.yangdb.fuse.core.driver.StandardQueryDriver;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.resource.store.LoggingResourceStore;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStoreFactory;
import com.yangdb.fuse.executor.elasticsearch.ClientProvider;
import com.yangdb.fuse.executor.elasticsearch.TimeoutClientAdvisor;
import com.yangdb.fuse.executor.elasticsearch.logging.LoggingClient;
import com.yangdb.fuse.executor.logging.LoggingCursorFactory;
import com.yangdb.fuse.executor.ontology.CachedGraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.executor.ontology.schema.*;
import com.yangdb.fuse.executor.resource.PersistantResourceStore;
import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.elasticsearch.client.Client;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.configuration.UniGraphConfiguration;

import java.util.List;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class ExecutorModule extends ModuleBase {
    public static final String globalClient = "ExecutorModule.@globalClient";

    //region Jooby.Module Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindResourceManager(env, conf, binder);
        bindInitialDataLoader(env, conf, binder);
        bindCursorFactory(env, conf, binder);
        bindElasticClient(env, conf, binder);
        bindRawSchema(env, conf, binder);
        bindSchemaProviderFactory(env, conf, binder);
        bindUniGraphProvider(env, conf, binder);

        binder.bind(QueryDriver.class).to(StandardQueryDriver.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(StandardCursorDriver.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(StandardPageDriver.class).in(RequestScoped.class);
        binder.bind(SearchOrderProviderFactory.class).to(getSearchOrderProvider(conf));
    }

    //endregion

    //region Private Methods

    protected void bindResourceManager(Env env, Config conf, Binder binder) {
        // resource store and persist processor
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(ResourceStoreFactory.injectionName))
                .to(PersistantResourceStore.class)
                .in(new SingletonScope());
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(LoggingResourceStore.injectionName))
                .to(ResourceStoreFactory.class)
                .in(new SingletonScope());
        binder.bind(ResourceStore.class)
                .to(LoggingResourceStore.class)
                .in(new SingletonScope());

    }

    protected void bindInitialDataLoader(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphDataLoader.class)
                            .to(getInitialDataLoader(conf));
                    this.expose(GraphDataLoader.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindRawSchema(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(RawSchema.class)
                            .annotatedWith(named(PrefixedRawSchema.rawSchemaParameter))
                            .to(getRawElasticSchemaClass(conf))
                            .asEagerSingleton();

                    String prefix = conf.hasPath(conf.getString("assembly") + ".physical_raw_schema_prefix") ?
                            conf.getString(conf.getString("assembly") + ".physical_raw_schema_prefix") :
                            "";
                    this.bindConstant().annotatedWith(named(PrefixedRawSchema.prefixParameter)).to(prefix);

                    this.bind(IndicesProvider.class)
                            .annotatedWith(named(CachedRawSchema.systemIndicesParameter))
                            .to(SystemIndicesProvider.class)
                            .asEagerSingleton();
                    this.bind(RawSchema.class)
                            .annotatedWith(named(PartitionFilteredRawSchema.rawSchemaParameter))
                            .to(PrefixedRawSchema.class)
                            .asEagerSingleton();

                    this.bind(RawSchema.class)
                            .annotatedWith(named(CachedRawSchema.rawSchemaParameter))
                            .to(PartitionFilteredRawSchema.class)
                            .asEagerSingleton();

                    this.bind(RawSchema.class).to(CachedRawSchema.class).asEagerSingleton();

                    this.expose(RawSchema.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindCursorFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CursorFactory.class)
                        .annotatedWith(named(LoggingCursorFactory.cursorFactoryParameter))
                        .to(CompositeCursorFactory.class)
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
            }
        });
    }

    protected void bindElasticClient(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                boolean createMock = conf.hasPath("fuse.elasticsearch.mock") && conf.getBoolean("fuse.elasticsearch.mock");
                ElasticGraphConfiguration elasticGraphConfiguration = createElasticGraphConfiguration(conf);
                this.bind(ElasticGraphConfiguration.class).toInstance(elasticGraphConfiguration);

                ClientProvider provider = new ClientProvider(createMock, elasticGraphConfiguration);
                Client client = provider.get();

                this.bindConstant()
                        .annotatedWith(named(ClientProvider.createMockParameter))
                        .to(createMock);

                this.bind(Client.class)
                        .annotatedWith(named(LoggingClient.clientParameter))
                        .toInstance(client);
                        //.toProvider(ClientProvider.class).asEagerSingleton();

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingClient.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingClient.class));
                this.bind(Client.class)
                        .to(TimeoutClientAdvisor.class)
                        .in(RequestScoped.class);

                this.bind(Client.class)
                        .annotatedWith(named(globalClient))
                        .toInstance(client);

                this.expose(Client.class);
                this.expose(Client.class).annotatedWith(named(globalClient));
                this.expose(ElasticGraphConfiguration.class);
            }
        });
    }

    protected void bindSchemaProviderFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .annotatedWith(named(OntologyGraphElementSchemaProviderFactory.schemaProviderFactoryParameter))
                            .to(getSchemaProviderFactoryClass(conf));
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .annotatedWith(named(CachedGraphElementSchemaProviderFactory.schemaProviderFactoryParameter))
                            .to(OntologyGraphElementSchemaProviderFactory.class);
                    /*this.bind(Logger.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.warnLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(GraphElementSchemaProvider.class));
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.verboseLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(GraphElementSchemaProvider.class.getName() + ".Verbose"));
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .to(LoggingGraphElementSchemaProviderFactory.class);*/

                    this.bind(GraphElementSchemaProviderFactory.class)
                            .to(CachedGraphElementSchemaProviderFactory.class)
                            .asEagerSingleton();

                    this.expose(GraphElementSchemaProviderFactory.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindUniGraphProvider(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(UniGraphConfiguration.class).toInstance(createUniGraphConfiguration(conf));
                    this.bind(UniGraphProvider.class)
                            .to(getUniGraphProviderClass(conf))
                            .in(RequestScoped.class);

                    this.expose(UniGraphProvider.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Class<? extends RawSchema> getRawElasticSchemaClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly")+".physical_raw_schema"));
    }

    private Class<? extends GraphDataLoader> getInitialDataLoader(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphDataLoader>) (Class.forName(conf.getString(conf.getString("assembly")+".physical_schema_data_loader")));
    }

    private Class<? extends SearchOrderProviderFactory> getSearchOrderProvider(Config conf) throws ClassNotFoundException {
        return (Class<? extends SearchOrderProviderFactory>) (Class.forName(conf.getString(conf.getString("assembly")+".search_order_provider")));
    }

    private ElasticGraphConfiguration createElasticGraphConfiguration(Config conf) {
        ElasticGraphConfiguration configuration = new ElasticGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(getStringList(conf, "elasticsearch.hosts")).toJavaArray(String.class));
        configuration.setClusterPort(conf.getInt("elasticsearch.port"));
        configuration.setClusterName(conf.getString("elasticsearch.cluster_name"));
        configuration.setElasticGraphDefaultSearchSize(conf.getLong("elasticsearch.default_search_size"));
        configuration.setElasticGraphMaxSearchSize(conf.getLong("elasticsearch.max_search_size"));
        configuration.setElasticGraphScrollSize(conf.getInt("elasticsearch.scroll_size"));
        configuration.setElasticGraphScrollTime(conf.getInt("elasticsearch.scroll_time"));

        configuration.setClientTransportIgnoreClusterName(conf.hasPath("client.transport.ignore_cluster_name") &&
                conf.getBoolean("client.transport.ignore_cluster_name"));
        
        return configuration;
    }

    private UniGraphConfiguration createUniGraphConfiguration(Config conf) {
        UniGraphConfiguration configuration = new UniGraphConfiguration();
        configuration.setBulkMax(conf.hasPath("unipop.bulk.max") ? conf.getInt("unipop.bulk.max") : 1000);
        configuration.setBulkMin(conf.hasPath("unipop.bulk.min") ? conf.getInt("unipop.bulk.min") : configuration.getBulkMax());
        configuration.setBulkDecayInterval(conf.hasPath("unipop.bulk.decayInterval") ? conf.getLong("unipop.bulk.decayInterval") : 200L);
        configuration.setBulkStart(conf.hasPath("unipop.bulk.start") ? conf.getInt("unipop.bulk.start") : configuration.getBulkMax());
        configuration.setBulkMultiplier(conf.hasPath("unipop.bulk.multiplier") ? conf.getInt("unipop.bulk.multiplier") : 1);
        return configuration;
    }

    protected Class<? extends GraphElementSchemaProviderFactory> getSchemaProviderFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphElementSchemaProviderFactory>) Class.forName(conf.getString(conf.getString("assembly")+".physical_schema_provider_factory_class"));
    }

    protected Class<? extends UniGraphProvider> getUniGraphProviderClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  UniGraphProvider>)Class.forName(conf.getString(conf.getString("assembly")+".unigraph_provider"));
    }

    protected Class<? extends CursorFactory> getCursorFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  CursorFactory>)Class.forName(conf.getString(conf.getString("assembly")+".cursor_factory"));
    }

    private List<String> getStringList(Config conf, String key) {
         try {
             return conf.getStringList(key);
         } catch (Exception ex) {
             String strList = conf.getString(key);
             return Stream.of(strList.split(",")).toJavaList();
         }
    }
    //endregion
}
