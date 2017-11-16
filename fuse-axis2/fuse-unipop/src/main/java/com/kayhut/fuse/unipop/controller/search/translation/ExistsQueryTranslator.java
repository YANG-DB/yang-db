package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.predicates.ExistsP;
import com.kayhut.fuse.unipop.predicates.NotExistsP;
import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by Roman on 18/05/2017.
 */
public class ExistsQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (predicate instanceof ExistsP) {
            return queryBuilder.push().exists(key).pop();
        }

        if (predicate instanceof NotExistsP) {
            return queryBuilder.push().bool().mustNot().exists(key).pop();
        }

        return queryBuilder;
    }
    //endregion
}
