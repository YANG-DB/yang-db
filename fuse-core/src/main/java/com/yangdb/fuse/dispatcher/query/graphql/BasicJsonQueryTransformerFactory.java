package com.yangdb.fuse.dispatcher.query.graphql;

import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import javaslang.Tuple2;

import java.util.List;

public class BasicJsonQueryTransformerFactory implements JsonQueryTransformerFactory {

    private List<Tuple2<String,QueryTransformer<String, AsgQuery>>> transformers;

    public BasicJsonQueryTransformerFactory(List<Tuple2<String,QueryTransformer<String, AsgQuery>>> transformers) {
        this.transformers = transformers;
    }

    @Override
    public QueryTransformer<String, AsgQuery> transform(String type) {
        return transformers.stream().filter(p->p._1.equals(type)).findAny().get()._2;
    }


}
