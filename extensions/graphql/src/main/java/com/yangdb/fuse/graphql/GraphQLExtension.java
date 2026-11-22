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

import com.google.inject.Module;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.graphql.services.GraphQLControllerRegistrar;
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;

import java.util.Collections;
import java.util.List;

/**
 * GraphQL for YANG-DB, as an extension: discovered through
 * {@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension}.
 * <ul>
 *     <li>the {@code graphQL} query language ({@code /fuse/query/graphQL}, {@code /fuse/query/graphQL/run})</li>
 *     <li>GraphQL SDL to ontology translation ({@code POST /fuse/graphql/ontology/{id}})</li>
 *     <li>the {@code graphQL} cursor</li>
 * </ul>
 */
public class GraphQLExtension implements YangDbExtension {
    @Override
    public String name() {
        return "graphql";
    }

    @Override
    public List<Module> modules() {
        return Collections.singletonList(new GraphQLModule());
    }

    @Override
    public List<AppRegistrar> registrars() {
        return Collections.singletonList(new GraphQLControllerRegistrar());
    }
}
