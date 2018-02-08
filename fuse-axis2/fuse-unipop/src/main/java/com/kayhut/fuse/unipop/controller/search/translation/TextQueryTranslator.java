package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.Text;

import java.util.Collections;
import java.util.List;

/**
 * Created by Roman on 18/05/2017.
 */
public class TextQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate.getBiPredicate() instanceof Text.TextPredicate)) {
            return queryBuilder;
        }

        Text.TextPredicate text = (Text.TextPredicate)predicate.getBiPredicate();
        switch (text) {
            case PREFIX:
                List<String> prefixes = CollectionUtil.listFromObjectValue(predicate.getValue());
                switch (prefixes.size()) {
                    case 0:
                        break;
                    case 1:
                        queryBuilder.push().prefix(key, prefixes.get(0)).pop();
                        break;
                    default:
                        queryBuilder.bool().should();
                        prefixes.forEach(prefix -> queryBuilder.push().prefix(key, prefix).pop());
                        queryBuilder.pop();
                }
                break;

            case REGEXP:
                List<String> regexs = CollectionUtil.listFromObjectValue(predicate.getValue());
                switch (regexs.size()) {
                    case 0:
                        break;
                    case 1:
                        queryBuilder.push().regexp(key, regexs.get(0)).pop();
                        break;
                    default:
                        queryBuilder.push().bool().should();
                        regexs.forEach(regex ->  queryBuilder.push().regexp(key, regex).pop());
                        queryBuilder.pop();
                }
                break;

            case LIKE:
                queryBuilder.push().wildcard(key, predicate.getValue().toString()).pop();
                break;
        }

        return queryBuilder;
    }
    //endregion
}
