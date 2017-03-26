package com.kayhut.fuse.unipop.schemaProviders;

import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

public class PrefixedIdEdgeRedundancy extends PrefixedEdgeRedundancy {
    //region Constructor
    public PrefixedIdEdgeRedundancy(GraphEdgeSchema.End end, String prefix) {
        super(prefix);
        this.end = end;
    }
    //endregion

    //region PrefixedEdgeRedundancy Implementation
    @Override
    public Optional<String> getRedundantPropertyName(String propertyName) {
        if (propertyName.equals(T.id.getAccessor())) {
            return Optional.of(this.end.getIdField());
        } else {
            return Optional.of(this.getPrefix() + propertyName);
        }
    }
    //endregion

    //region Fields
    GraphEdgeSchema.End end;
    //endregion
}
