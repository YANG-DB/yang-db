package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 11/05/2017.
 */
public class DragonsPhysicalIndexProvider implements PhysicalIndexProvider {
    //region Constructors
    public DragonsPhysicalIndexProvider() {
        this.indexPartitions = new HashMap<>();
        this.indexPartitions.put("Person", new StaticIndexPartition(Arrays.asList("person")));
        this.indexPartitions.put("Dragon", new StaticIndexPartition(Arrays.asList("dragon")));
        this.indexPartitions.put("Fire", new StaticIndexPartition(Arrays.asList("fire20170511", "fire20170512", "fire20170513")));
    }
    //endregion

    //region PhysicalIndexProvider Implementation
    @Override
    public IndexPartition getIndexPartitionByLabel(String label, ElementType elementType) {
        return this.indexPartitions.get(label);
    }
    //endregion

    //region Fields
    private Map<String, IndexPartition> indexPartitions;
    //endregion
}
