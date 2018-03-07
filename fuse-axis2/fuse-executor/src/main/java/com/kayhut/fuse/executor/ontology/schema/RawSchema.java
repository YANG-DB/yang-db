package com.kayhut.fuse.executor.ontology.schema;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by lior.perry on 2/11/2018.
 *
 * Describing the elastic (raw) indices & indices partitions
 * each index has id formatting
 */
public interface RawSchema {
    IndexPartitions getPartition(String type);

    String getIdFormat(String type);

    List<IndexPartitions.Partition> getPartitions(String type);

    Iterable<String> indices();
}
