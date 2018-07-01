package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Collections;

/**
 * Created by Roman on 18/05/2017.
 */
public class ContainsQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate.getBiPredicate() instanceof Contains)) {
            return queryBuilder;
        }

        Contains contains = (Contains) predicate.getBiPredicate();
        switch (contains) {
            case within:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().bool().mustNot().exists(key).pop();
                } else {
                    queryBuilder.push().terms(key, predicate.getValue()).pop();
                }
                break;
            case without:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().exists(key).pop();
                } else {
                    queryBuilder.push().bool().mustNot().terms(key, predicate.getValue()).pop();
                }
                break;
        }

        return queryBuilder;
    }
    //endregion
}
