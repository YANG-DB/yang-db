package com.yangdb.fuse.asg;

/*-
 * #%L
 * fuse-asg
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


import com.google.inject.Inject;
import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQL2QueryTransformer;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * Created by liorp on 12/15/2017.
 */
public class AsgGraphQLTransformer implements AsgTranslator<String,AsgQuery> {
    private Ontology ontology;
    private final QueryToAsgTransformer queryTransformer;
    private final TypeDefinitionRegistry registry;

    //region Constructors
    @Inject
    public AsgGraphQLTransformer(Ontology ontology,QueryToAsgTransformer queryTransformer, TypeDefinitionRegistry registry) {
        this.ontology = ontology;
        this.queryTransformer = queryTransformer;
        this.registry = registry;
    }
    //endregion

    //region QueryTransformer Implementation

    @Override
    public AsgQuery translate(String query) {
        Query transform = GraphQL2QueryTransformer.transform(registry,ontology, query);
        return queryTransformer.transform(transform);
    }

    //endregion

}
