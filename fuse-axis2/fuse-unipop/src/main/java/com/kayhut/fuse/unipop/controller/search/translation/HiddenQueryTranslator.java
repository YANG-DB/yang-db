package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * Created by Roman on 18/05/2017.
 */
public class HiddenQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public HiddenQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }

    public HiddenQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }
    //endregion

    //region Override Methods
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        String plainKey = Graph.Hidden.isHidden(key) ? Graph.Hidden.unHide(key) : key;
        String newKey;

        switch (plainKey) {
            case "id":
                newKey = "_id";
                break;

            case "label":
                newKey = "type";
                break;

            default:
                newKey = plainKey;
        }

        return super.translate(queryBuilder, newKey, predicate);
    }
    //endregion
}
