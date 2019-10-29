package com.yangdb.fuse.services.modules;

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

/*-
 *
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.SingletonScope;
import com.google.inject.name.Names;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.cursor.CreateCursorRequestDeserializer;
import com.yangdb.fuse.dispatcher.driver.DashboardDriver;
import com.yangdb.fuse.dispatcher.driver.InternalsDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.resource.store.NodeStatusResource;
import com.yangdb.fuse.executor.resource.InMemNodeStatusResource;
import com.yangdb.fuse.logging.StatusReportedJob;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.execution.plan.descriptors.JacksonQueryDescriptor;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.ExecutionScope;
import com.yangdb.fuse.model.transport.PlanTraceOptions;
import com.yangdb.fuse.model.transport.cursor.*;
import com.yangdb.fuse.services.FuseUtils;
import com.yangdb.fuse.services.controllers.*;
import com.yangdb.fuse.services.controllers.logging.*;
import com.yangdb.fuse.services.suppliers.CachedRequestIdSupplier;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import com.yangdb.fuse.services.suppliers.SnowflakeRequestIdSupplier;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 15/02/2017.
 * <p>
 * This module is called by the fuse-service scanner class loader
 */
public class ServiceModule extends ModuleBase {
    private static final Logger logger = LoggerFactory.getLogger(ServiceModule.class);

    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        // bind common components
        long defaultTimeout = config.hasPath("fuse.cursor.timeout") ? config.getLong("fuse.cursor.timeout") : 60 * 1000 * 3;
        binder.bindConstant().annotatedWith(Names.named(ExecutionScope.clientParameter)).to(defaultTimeout);
        binder.bind(RequestIdSupplier.class)
                .annotatedWith(named(CachedRequestIdSupplier.RequestIdSupplierParameter))
                .to(SnowflakeRequestIdSupplier.class)
                .asEagerSingleton();
        binder.bind(RequestIdSupplier.class).to(CachedRequestIdSupplier.class).in(RequestScoped.class);

        binder.bind(RequestExternalMetadataSupplier.class).to(RequestExternalMetadataSupplier.Impl.class).in(RequestScoped.class);

        // bind service controller
        bindApiDescriptionController(env, config, binder);
        bindInternalsController(env, config, binder);
        bindDashboardController(env, config, binder);
        bindQueryController(env, config, binder);
        bindCursorController(env, config, binder);
        bindPageController(env, config, binder);
        bindCatalogController(env, config, binder);
        bindDataLoaderController(env, config, binder);
        bindIdGeneratorController(env, config, binder);

        // bind requests
        binder.bind(InternalsDriver.class).to(StandardInternalsDriver.class).in(RequestScoped.class);
        binder.bind(DashboardDriver.class).to(StandardDashboardDriver.class).in(RequestScoped.class);
        binder.bind(CreateQueryRequest.class).in(RequestScoped.class);
        binder.bind(CreatePageRequest.class).in(RequestScoped.class);
        //cursors type
        binder.bind(CreateCsvCursorRequest.class).in(RequestScoped.class);
        binder.bind(CreateGraphHierarchyCursorRequest.class).in(RequestScoped.class);
        binder.bind(CreateGraphCursorRequest.class).in(RequestScoped.class);
        binder.bind(CreatePathsCursorRequest.class).in(RequestScoped.class);
        //execution scope
        binder.bind(ExecutionScope.class).in(RequestScoped.class);

        //bind request parameters
        binder.bind(PlanTraceOptions.class).in(RequestScoped.class);

        //bind status resource
        bindStatusResource(env, config, binder);
        // register PostConfigurer
        binder.bind(PostConfigurer.class).asEagerSingleton();
        //register Status Reported Job
        binder.bind(StatusReportedJob.class).in(new SingletonScope());
        //register life cycle hooks
        processLifeCycle(env, config, binder);
    }

    private void processLifeCycle(Env env, Config config, Binder binder) {
        env.onStart(() -> {
            logger.info("starting Fuse");
            FuseUtils.onStart();
        });

        env.onStop(() -> {
            logger.info("stopping Fuse");
            FuseUtils.onStop();
        });

        env.onStarted(() -> {
            logger.info("Fuse started");
            FuseUtils.onStarted();
        });

    }
    //endregion

    //region Private Methods
    private void bindApiDescriptionController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(ApiDescriptionController.class)
                        .annotatedWith(named(LoggingApiDescriptionController.controllerParameter))
                        .to(StandardApiDescriptionController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingApiDescriptionController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardApiDescriptionController.class));

                this.bind(ApiDescriptionController.class)
                        .to(LoggingApiDescriptionController.class);

                this.expose(ApiDescriptionController.class);
            }
        });
    }

    protected void bindStatusResource(Env env, Config conf, Binder binder) {
        // node status persist processor
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                String clazz = env.config().hasPath("fuse.node_status_reporter") ?
                        env.config().getString("fuse.node_status_reporter") :
                        InMemNodeStatusResource.class.getName();
                try {
                    this.bind(NodeStatusResource.class)
                            .to((Class<? extends NodeStatusResource>) Class.forName(clazz))
                            .asEagerSingleton();
                    this.expose(NodeStatusResource.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void bindInternalsController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(InternalsController.class)
                        .to(StandardInternalsController.class);
                this.expose(InternalsController.class);
            }
        });
    }

    private void bindDashboardController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(DashboardController.class)
                        .to(StandardDashboardController.class);
                this.expose(DashboardController.class);
            }
        });
    }

    private void bindQueryController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CursorController.class)
                        .annotatedWith(named(StandardQueryController.cursorControllerParameter))
                        .to(StandardCursorController.class);

                this.bind(PageController.class)
                        .annotatedWith(named(StandardQueryController.pageControllerParameter))
                        .to(StandardPageController.class);

                this.bind(QueryController.class)
                        .annotatedWith(named(LoggingQueryController.controllerParameter))
                        .to(StandardQueryController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingQueryController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardQueryController.class));

                this.bind(new TypeLiteral<Descriptor<Query>>() {
                })
                        .annotatedWith(named(LoggingQueryController.queryDescriptorParameter))
                        .to(JacksonQueryDescriptor.class).asEagerSingleton();

                this.bind(QueryController.class)
                        .to(LoggingQueryController.class);

                this.expose(QueryController.class);
            }
        });
    }

    private void bindCursorController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CursorController.class)
                        .annotatedWith(named(LoggingCursorController.controllerParameter))
                        .to(StandardCursorController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingCursorController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardCursorController.class));

                this.bind(CursorController.class)
                        .to(LoggingCursorController.class);

                this.expose(CursorController.class);
            }
        });
    }

    private void bindPageController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(PageController.class)
                        .annotatedWith(named(LoggingPageController.controllerParameter))
                        .to(StandardPageController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingPageController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardPageController.class));

                this.bind(PageController.class)
                        .to(LoggingPageController.class);

                this.expose(PageController.class);
            }
        });
    }

    private void bindCatalogController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(CatalogController.class)
                        .annotatedWith(named(LoggingCatalogController.controllerParameter))
                        .to(StandardCatalogController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingCatalogController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardCatalogController.class));

                this.bind(CatalogController.class)
                        .to(LoggingCatalogController.class);

                this.expose(CatalogController.class);
            }
        });
    }

    private void bindDataLoaderController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(DataLoaderController.class)
                        .annotatedWith(named(LoggingDataLoaderController.controllerParameter))
                        .to(StandardDataLoaderController.class);

                this.bind(Logger.class)
                        .annotatedWith(named(LoggingDataLoaderController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardDataLoaderController.class));

                this.bind(DataLoaderController.class)
                        .to(LoggingDataLoaderController.class);

                this.expose(DataLoaderController.class);
            }
        });
    }

    private void bindIdGeneratorController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {

                this.bind(new TypeLiteral<IdGeneratorController<Range>>() {
                })
                        .to(new TypeLiteral<StandardIdGeneratorController<Range>>() {
                        })
                        .asEagerSingleton();

                this.expose(new TypeLiteral<IdGeneratorController<Range>>() {
                });
            }
        });
    }
    //endregion

    private static class PostConfigurer {
        @Inject
        public PostConfigurer(ObjectMapper mapper, Set<CompositeCursorFactory.Binding> cursorBindings) {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(CreateCursorRequest.class, new CreateCursorRequestDeserializer(cursorBindings));
            mapper.registerModules(module);
        }
    }
}
