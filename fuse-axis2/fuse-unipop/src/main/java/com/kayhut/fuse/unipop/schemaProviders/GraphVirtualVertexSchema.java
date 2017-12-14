package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.Optional;

/**
 * Created by roman.margolis on 13/12/2017.
 */
public interface GraphVirtualVertexSchema extends GraphVertexSchema {
    class Impl extends GraphVertexSchema.Impl implements GraphVirtualVertexSchema {
        //region Constructors
        public Impl(String label) {
            super(label, Optional.empty(), Optional.empty());
        }
        //endregion
    }
}
