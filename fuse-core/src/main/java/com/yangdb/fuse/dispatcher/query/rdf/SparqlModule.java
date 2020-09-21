
package com.yangdb.fuse.dispatcher.query.rdf;

/*-
 * #%L
 * fuse-core
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


import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQL2OntologyTransformer;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQL2QueryTransformer;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQLSchemaUtils;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import org.jooby.Env;

import java.util.List;

/**
 * Created by lior.perry on 15/02/2017.
 * <p>
 * This module is called by the fuse-service scanner class loader
 */
public class SparqlModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<OntologyTransformerIfc<List<String>, Ontology>>(){})
                .to(OWL2OntologyTransformer.class)
                .asEagerSingleton();
    }

    //endregion
}