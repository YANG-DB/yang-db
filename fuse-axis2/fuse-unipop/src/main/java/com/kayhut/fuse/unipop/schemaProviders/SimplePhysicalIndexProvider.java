package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Arrays;

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
    public Iterable<IndexPartition> getIndexPartitionsByLabel(String label, ElementType elementType) {
        switch (elementType) {
            case edge: return Arrays.asList(() -> Arrays.asList(this.edgeIndexName));
            case vertex: return Arrays.asList(() -> Arrays.asList(this.vertexIndexName));
            default: return null;
        }
    }
    //endregion

    //region Fields
    private String vertexIndexName;
    private String edgeIndexName;
    //endregion
}
