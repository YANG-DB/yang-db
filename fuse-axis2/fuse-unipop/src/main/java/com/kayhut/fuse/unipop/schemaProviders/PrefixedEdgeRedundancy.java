package com.kayhut.fuse.unipop.schemaProviders;

import java.util.Optional;

/**
 * Created by k on 11/23/2015.
 */
public class PrefixedEdgeRedundancy implements GraphEdgeRedundancy {
    //region Constructor
    public PrefixedEdgeRedundancy(String prefix) {
        this.prefix = prefix;
    }
    //endregion

    //region GraphEdgeRedundancy Implementation
    @Override
    public Optional<String> getRedundantPropertyName(String propertyName) {
        return Optional.of(this.getPrefix() + propertyName);
    }
    //endregion

    //region Properties
    public String getPrefix() {
        return prefix;
    }
    //endregion

    //region Fields
    private String prefix;
    //endregion
}
