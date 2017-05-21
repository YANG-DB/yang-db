package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;

/**
 * Created by Roman on 18/05/2017.
 */
public class AndPQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public AndPQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }

    public AndPQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }
    //endregion

    //region CompositeQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate instanceof AndP<?>)) {
            return queryBuilder;
        }

        AndP<?> andP = (AndP<?>)predicate;
        for(P<?> innerPredicate : andP.getPredicates()) {
            queryBuilder.push();
            queryBuilder = super.translate(queryBuilder, key, innerPredicate);
            queryBuilder.pop();
        }

        return queryBuilder;
    }
    //endregion
}
