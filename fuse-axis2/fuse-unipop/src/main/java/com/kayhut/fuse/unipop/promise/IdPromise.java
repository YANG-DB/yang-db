package com.kayhut.fuse.unipop.promise;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Optional;

/**
 * Created by User on 07/03/2017.
 */
public class IdPromise implements Promise {
    //region Constructor
    public IdPromise(Object id) {
        this.id = id;
        this.label = Optional.empty();
    }

    public IdPromise(Object id, String label) {
        this(id);

        if (label == null || label.equals("")) {
            this.label = Optional.empty();
        } else {
            this.label = Optional.of(label);
        }
    }
    //endregion

    //region Promise Implementation
    @Override
    public Object getId() {
        return id;
    }
    //endregion

    //region Properties
    public Optional<String> getLabel() {
        return this.label;
    }
    //endregion

    //region Modulation
    public TraversalPromise by(Traversal traversal) {
        return new TraversalPromise(this.id, traversal);
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return label.isPresent() ?
                "Promise.as(" + getId().toString() + ", " + getLabel().get() + ")" :
                "Promise.as(" + getId().toString() + ")";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    //endregion

    //region fields
    private Object id;
    private Optional<String> label;
    //endregion
}
