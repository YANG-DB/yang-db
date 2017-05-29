package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.model.OntologyTestUtils.*;

/**
 * Created by Roman on 11/05/2017.
 */
public class DragonsPhysicalIndexProvider implements PhysicalIndexProvider {
    //region Constructors
    public DragonsPhysicalIndexProvider() {
        this.indexPartitions = new HashMap<>();
        this.indexPartitions.put(PERSON.name, new StaticIndexPartition(Arrays.asList(PERSON.name.toLowerCase())));
        this.indexPartitions.put(DRAGON.name, new StaticIndexPartition(Arrays.asList(DRAGON.name.toLowerCase())));
        this.indexPartitions.put(FIRE.getName(), new StaticIndexPartition(Arrays.asList(
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513")));
    }
    //endregion

    //region PhysicalIndexProvider Implementation
    @Override
    public IndexPartition getIndexPartitionByLabel(String label, ElementType elementType) {
        return this.indexPartitions.getOrDefault(label, emptyIndexPartition);
    }
    //endregion

    //region Fields
    private Map<String, IndexPartition> indexPartitions;
    private IndexPartition emptyIndexPartition = new StaticIndexPartition(Collections.emptyList());
    //endregion
}
