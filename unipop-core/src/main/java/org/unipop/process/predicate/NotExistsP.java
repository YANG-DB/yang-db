package org.unipop.process.predicate;

import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by roman.margolis on 23/11/2017.
 */
public class NotExistsP<V> extends P<V> {
    //region Constructors
    public NotExistsP() {
        super(null, null);
    }
    //endregion

    //region Override Methods
    @Override
    public P<V> negate() {
        return new ExistsP<V>();
    }

    @Override
    public String toString() {
        return "notExists";
    }

    @Override
    public int hashCode() {
        return "notExists".hashCode();
    }
    //endregion
}