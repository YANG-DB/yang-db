package com.kayhut.fuse.unipop.schemaProviders;

import java.util.Optional;

/**
 * Created by k on 11/23/2015.
 */
public interface GraphEdgeRedundancy {
    Optional<String> getRedundantPropertyName(String propertyName);
}
