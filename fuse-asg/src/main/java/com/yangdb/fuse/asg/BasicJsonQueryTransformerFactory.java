package com.yangdb.fuse.asg;

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import javaslang.Tuple2;

import java.util.List;

public class BasicJsonQueryTransformerFactory implements JsonQueryTransformerFactory {

    private AsgCypherTransformer cypherTransformer;
    private AsgGraphQLTransformer graphQueryTransformer;

    @Inject
    public BasicJsonQueryTransformerFactory(AsgCypherTransformer cypherTransformer, AsgGraphQLTransformer graphQueryTransformer) {
        this.cypherTransformer = cypherTransformer;
        this.graphQueryTransformer = graphQueryTransformer;
    }


    @Override
    public QueryTransformer<QueryInfo<String>, AsgQuery> transform(String type) {
        switch (type) {
            case CreateQueryRequestMetadata.TYPE_CYPHER:
                return cypherTransformer;
            case CreateQueryRequestMetadata.TYPE_GRAPH_QL:
                return graphQueryTransformer;
        }
        throw new FuseError.FuseErrorException(new FuseError("No Query translator found","No matching json query translator found for type "+type));
    }


}
