package com.yangdb.fuse.graphql;

/*-
 * #%L
 * fuse-extension-graphql
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

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory.Binding;
import com.yangdb.fuse.extension.QueryLanguages;
import com.yangdb.fuse.graphql.asg.AsgGraphQLTransformer;
import com.yangdb.fuse.graphql.cursor.CreateGraphQLCursorRequest;
import com.yangdb.fuse.graphql.cursor.GraphQLTraversalCursor;
import com.yangdb.fuse.graphql.services.controllers.LoggingGraphQLController;
import com.yangdb.fuse.graphql.services.controllers.StandardGraphQLController;
import com.yangdb.fuse.graphql.translator.GraphQL2QueryTransformer;
import com.yangdb.fuse.graphql.translator.GraphQLSchemaUtils;
import com.yangdb.fuse.graphql.translator.GraphQLToOntologyTransformer;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.services.controllers.SchemaTranslatorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

/**
 * The GraphQL extension's bindings: the {@code graphQL} query language, the schema translator, the {@code graphQL}
 * cursor and the (logging) schema translator controller. A plain Guice module, installed by {@link GraphQLExtension}:
 * nothing needs to be listed in the application config.
 */
public class GraphQLModule extends AbstractModule {
    @Override
    protected void configure() {
        // schema translation (SDL <-> ontology)
        bind(GraphQLToOntologyTransformer.class).in(Singleton.class);
        bind(GraphQLSchemaUtils.class).to(GraphQLToOntologyTransformer.class);
        bind(GraphQL2QueryTransformer.class).in(Singleton.class);

        // the query language
        QueryLanguages.register(binder(), CreateQueryRequestMetadata.TYPE_GRAPHQL).to(AsgGraphQLTransformer.class);

        // the cursor
        Multibinder.newSetBinder(binder(), Binding.class).addBinding().toInstance(new Binding(
                CreateGraphQLCursorRequest.CursorType,
                CreateGraphQLCursorRequest.class,
                new GraphQLTraversalCursor.Factory()));

        // the schema translator controller, with its logging decorator (was ServiceModule.bindGraphQLController)
        install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(SchemaTranslatorController.class)
                        .annotatedWith(named(LoggingGraphQLController.controllerParameter))
                        .to(StandardGraphQLController.class);
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingGraphQLController.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(StandardGraphQLController.class));
                this.bind(SchemaTranslatorController.class)
                        .annotatedWith(named(StandardGraphQLController.transformerName))
                        .to(LoggingGraphQLController.class);
                this.expose(SchemaTranslatorController.class)
                        .annotatedWith(named(StandardGraphQLController.transformerName));
            }
        });
    }
}
