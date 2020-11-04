package com.yangdb.fuse.asg.translator;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.yangdb.fuse.asg.translator.cypher.AsgCypherTransformer;
import com.yangdb.fuse.asg.translator.graphql.AsgGraphQLTransformer;
import com.yangdb.fuse.asg.translator.sparql.AsgSparQLTransformer;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;

public class BasicJsonQueryTransformerFactory implements JsonQueryTransformerFactory {

    private AsgCypherTransformer cypherTransformer;
    private AsgGraphQLTransformer graphQueryTransformer;
    private AsgSparQLTransformer sparQLTransformer;

    @Inject
    public BasicJsonQueryTransformerFactory(AsgCypherTransformer cypherTransformer,
                                            AsgSparQLTransformer sparQLTransformer,
                                            AsgGraphQLTransformer graphQueryTransformer) {
        this.cypherTransformer = cypherTransformer;
        this.sparQLTransformer = sparQLTransformer;
        this.graphQueryTransformer = graphQueryTransformer;
    }


    @Override
    public QueryTransformer<QueryInfo<String>, AsgQuery> transform(String type) {
        switch (type) {
            case CreateQueryRequestMetadata.TYPE_SPARQL:
                return sparQLTransformer;
            case CreateQueryRequestMetadata.TYPE_CYPHERQL:
                return cypherTransformer;
            case CreateQueryRequestMetadata.TYPE_GRAPHQL:
                return graphQueryTransformer;
        }
        throw new FuseError.FuseErrorException(new FuseError("No Query translator found","No matching json query translator found for type "+type));
    }


}
