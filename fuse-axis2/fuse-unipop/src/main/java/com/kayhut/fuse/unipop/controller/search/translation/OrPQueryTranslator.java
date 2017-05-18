package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;

/**
 * Created by Roman on 18/05/2017.
 */
public class OrPQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public OrPQueryTranslator(PredicateQueryTranslator...translators) {
        super(null, translators);
    }

    public OrPQueryTranslator(String name, PredicateQueryTranslator...translators) {
        super(name, Stream.of(translators).toJavaList());
    }

    public OrPQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(null, translators);
    }

    public OrPQueryTranslator(String name, Iterable<PredicateQueryTranslator> translators) {
        super(name, translators);
    }
    //endregion

    //region CompositeQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate instanceof OrP<?>)) {
            return queryBuilder;
        }

        OrP<?> orP = (OrP<?>)predicate;
        queryBuilder.push().bool().should();
        for(P<?> innerPredicate : orP.getPredicates()) {
            queryBuilder.push();
            queryBuilder = super.translate(queryBuilder, key, innerPredicate);
            queryBuilder.pop();
        }

        return queryBuilder.pop();
    }
    //endregion
}
