package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by Roman on 06/04/2017.
 */
public class SimplePhysicalIndexProvider implements PhysicalIndexProvider {
    //region Constructors
    public SimplePhysicalIndexProvider(String vertexIndexName, String edgeIndexName) {
        this.vertexIndexName = vertexIndexName;
        this.edgeIndexName = edgeIndexName;
    }
    //endregion

    //region PhysicalIndexProvider Implementation
    @Override
    public IndexPartitions getIndexPartitionsByLabel(String label, ElementType elementType) {
        switch (elementType) {
            case edge: return new StaticIndexPartitions(Collections.singletonList(this.edgeIndexName));
            case vertex: return new StaticIndexPartitions(Collections.singletonList(this.vertexIndexName));
            default: return null;
        }
    }
    //endregion

    //region Fields
    private String vertexIndexName;
    private String edgeIndexName;
    //endregion
}
