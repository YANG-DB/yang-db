package com.yangdb.fuse.executor;

/*-
 * #%L
 * fuse-dv-core
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

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.ElasticsearchExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ExecutionProtector;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.NoopExecutionProtector;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.sql.SQLService;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.internal.SingletonScope;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.yangdb.fuse.client.export.GraphWriterStrategy;
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
import com.yangdb.fuse.executor.elasticsearch.terms.LoggingTermGraphExploration;
import com.yangdb.fuse.executor.elasticsearch.terms.TermGraphExploration;
import com.yangdb.fuse.executor.elasticsearch.terms.TermGraphExplorationDriver;
import com.yangdb.fuse.executor.logging.LoggingCursorFactory;
import com.yangdb.fuse.executor.ontology.CachedGraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.executor.ontology.schema.*;
import com.yangdb.fuse.executor.ontology.schema.load.CSVDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphInitiator;
import com.yangdb.fuse.executor.resource.PersistentResourceStore;
import com.yangdb.fuse.executor.sql.ElasticsearchFuseClient;
import com.yangdb.fuse.executor.sql.FuseSqlService;
import com.yangdb.fuse.executor.sql.FuseStorageEngine;
import com.yangdb.fuse.executor.sql.LoggingElasticsearchFuseClient;
import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.configuration.UniGraphConfiguration;

import static com.google.inject.name.Names.named;
import static com.yangdb.fuse.executor.utils.ConfigUtils.createElasticGraphConfiguration;
import static org.elasticsearch.common.settings.Settings.EMPTY;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class ExecutorModule extends ModuleBase {
    public static final String globalClient = "ExecutorModule.@globalClient";

    //region Jooby.Module Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindGraphWriters(env, conf, binder);
        bindResourceManager(env, conf, binder);
        bindGraphInitiator(env, conf, binder);
        bindGraphDataLoader(env, conf, binder);
        bindCSVDataLoader(env, conf, binder);
        bindCursorFactory(env, conf, binder);
        bindElasticClient(env, conf, binder);
        bindRawSchema(env, conf, binder);
        bindSchemaProviderFactory(env, conf, binder);
        bindUniGraphProvider(env, conf, binder);
        bindSqlExecutor(env,conf,binder);
        bindTermExplorer(env,conf,binder);

        binder.bind(QueryDriver.class).to(StandardQueryDriver.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(StandardCursorDriver.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(StandardPageDriver.class).in(RequestScoped.class);

        binder.bind(SearchOrderProviderFactory.class).to(getSearchOrderProvider(conf));

    }

    private void bindTermExplorer(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(TermGraphExploration.class)
                        .annotatedWith(named(LoggingTermGraphExploration.clientParam))
                        .to(TermGraphExplorationDriver.class);
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingTermGraphExploration.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingTermGraphExploration.class));
                this.bind(TermGraphExploration.class)
                        .to(LoggingTermGraphExploration.class);

                this.expose(TermGraphExploration.class);
            }
        });

    }

    private void bindSqlExecutor(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                //todo - move to singletone ?
                this.bind(ElasticsearchClient.class)
                        .annotatedWith(named(LoggingElasticsearchFuseClient.clientParam))
                        .to(ElasticsearchFuseClient.class);
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingElasticsearchFuseClient.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingElasticsearchFuseClient.class));
                this.bind(ElasticsearchClient.class)
                        .to(LoggingElasticsearchFuseClient.class);

                this.expose(ElasticsearchClient.class);
            }
        });

        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                ExpressionConfig expressionConfig = new ExpressionConfig();

                binder.bind(Settings.class)
                        .toInstance(new Settings() {
                            @Override
                            public Object getSettingValue(Key key) {
                                if (Key.QUERY_SIZE_LIMIT.equals(key)) {
                                    return 200;
                                }
                                if (Settings.Key.PPL_QUERY_MEMORY_LIMIT.equals(key)) {
                                    return new ByteSizeValue(1, ByteSizeUnit.GB);
                                }
                                return EMPTY;
                            }
                        });

                binder.bind(ExecutionProtector.class)
                        .toInstance(new NoopExecutionProtector());
                binder.bind(BuiltinFunctionRepository.class)
                        .toInstance(expressionConfig.functionRepository());
                binder.bind(DSL.class)
                        .toInstance(expressionConfig.dsl(expressionConfig.functionRepository()));
                binder.bind(StorageEngine.class)
                        .to(FuseStorageEngine.class);
                binder.bind(ExecutionEngine.class)
                        .to(ElasticsearchExecutionEngine.class);
                binder.bind(SQLService.class);
                binder.bind(FuseSqlService.class);

            }
        });

    }


    //endregion

    //region Private Methods
    protected void bindGraphWriters(Env env, Config conf, Binder binder) {
        binder.bind(GraphWriterStrategy.class).toInstance(new GraphWriterStrategy());
    }

    protected void bindResourceManager(Env env, Config conf, Binder binder) {
        // resource store and persist processor
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(ResourceStoreFactory.injectionName))
                .to(PersistentResourceStore.class)
                .in(new SingletonScope());
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(LoggingResourceStore.injectionName))
                .to(ResourceStoreFactory.class)
                .in(new SingletonScope());
        binder.bind(ResourceStore.class)
                .to(LoggingResourceStore.class)
                .in(new SingletonScope());

    }

    protected void bindGraphInitiator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphInitiator.class)
                            .to(getGraphInitiator(conf));
                    this.expose(GraphInitiator.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindGraphDataLoader(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphDataLoader.class)
                            .to(getGraphDataLoader(conf));
                    this.expose(GraphDataLoader.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindCSVDataLoader(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(CSVDataLoader.class)
                            .to(getCSVDataLoader(conf));
                    this.expose(CSVDataLoader.class);

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
                this.bindConstant()
                        .annotatedWith(named(ClientProvider.createMockParameter))
                        .to(createMock);

                ElasticGraphConfiguration elasticGraphConfiguration = createElasticGraphConfiguration(conf);
                this.bind(ElasticGraphConfiguration.class).toInstance(elasticGraphConfiguration);

                ClientProvider provider = new ClientProvider(createMock, elasticGraphConfiguration);
                Client client = provider.get();


                this.bind(Client.class)
                        .annotatedWith(named(LoggingClient.clientParameter))
                        .toInstance(client);
                //.toProvider(ClientProvider.class).asEagerSingleton();

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingClient.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingClient.class));

                // ToDo - remove timeout scope for E/S client execution due to the toolkit provided by new E/S API
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
        return (Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_raw_schema"));
    }

    private Class<? extends GraphDataLoader> getGraphDataLoader(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphDataLoader>) (Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_data_loader")));
    }

    private Class<? extends GraphInitiator> getGraphInitiator(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphInitiator>) (Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_initiator")));
    }

    private Class<? extends CSVDataLoader> getCSVDataLoader(Config conf) throws ClassNotFoundException {
        return (Class<? extends CSVDataLoader>) (Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_csv_data_loader")));
    }

    private Class<? extends SearchOrderProviderFactory> getSearchOrderProvider(Config conf) throws ClassNotFoundException {
        return (Class<? extends SearchOrderProviderFactory>) (Class.forName(conf.getString(conf.getString("assembly") + ".search_order_provider")));
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
        return (Class<? extends GraphElementSchemaProviderFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_schema_provider_factory_class"));
    }

    protected Class<? extends UniGraphProvider> getUniGraphProviderClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends UniGraphProvider>) Class.forName(conf.getString(conf.getString("assembly") + ".unigraph_provider"));
    }

    protected Class<? extends CursorFactory> getCursorFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends CursorFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".cursor_factory"));
    }

    //endregion
}
