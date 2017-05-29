package com.kayhut.fuse.unipop.predicates;

import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.function.BiPredicate;

/**
 * Created by Roman on 24/05/2017.
 */
public enum SelectP implements BiPredicate {
    raw {
        @Override
        public boolean test(Object o, Object o2) {
            return true;
        }
    };

    //region Static
    public static <V> P<V> raw(V name) {
        return new P<V>(SelectP.raw, name);
    }
    //endregion
}
