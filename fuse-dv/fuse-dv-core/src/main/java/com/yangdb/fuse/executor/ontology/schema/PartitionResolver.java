package com.yangdb.fuse.executor.ontology.schema;

import com.google.common.collect.Lists;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;

import java.util.List;

public interface PartitionResolver {

    IndexPartitions getPartition(String type);

    List<IndexPartitions.Partition> getPartitions(String type);


    class StaticPartitionResolver implements PartitionResolver {
        private StaticIndexPartitions indexPartitions;

        public StaticPartitionResolver(String... indices) {
            this.indexPartitions = new StaticIndexPartitions(indices);
        }

        @Override
        public IndexPartitions getPartition(String type) {
            return indexPartitions;
        }

        @Override
        public List<IndexPartitions.Partition> getPartitions(String type) {
            return Lists.newArrayList(indexPartitions.getPartitions());
        }
    }
}
