package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import com.kayhut.fuse.unipop.structure.*;


/**
 * Created by benishue on 23-Mar-17.
 */
public interface PhysicalIndexProvider {
    class Constant implements PhysicalIndexProvider {
        //region Constructors
        public Constant(IndexPartition indexPartition) {
            this.indexPartition = indexPartition;
        }
        //endregion

        //region PhysicalIndexProvider Implementation
        @Override
        public IndexPartition getIndexPartitionByLabel(String label, ElementType elementType) {
            return this.indexPartition;
        }
        //endregion

        //region Fields
        private IndexPartition indexPartition;
        //endregion
    }

    IndexPartition getIndexPartitionByLabel(String label, ElementType elementType);
}
