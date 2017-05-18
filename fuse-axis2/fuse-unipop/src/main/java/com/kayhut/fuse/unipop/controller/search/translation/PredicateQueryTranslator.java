package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

import java.util.function.Predicate;

/**
 * Created by Roman on 18/05/2017.
 */
public interface PredicateQueryTranslator {
    QueryBuilder translate(QueryBuilder queryBuilder, String key, P predicate);
}
