package org.unipop.process.predicate;


import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by Roman on 11/9/2015.
 */
public class ExistsP<V> extends P<V> {
    //region Constructors
    public ExistsP() {
        super(null, null);
    }
    //endregion

    //region Override Methods
    @Override
    public P<V> negate() {
        return new NotExistsP<V>();
    }

    @Override
    public String toString() {
        return "exists";
    }

    @Override
    public int hashCode() {
        return "exists".hashCode();
    }
    //endregion
}
