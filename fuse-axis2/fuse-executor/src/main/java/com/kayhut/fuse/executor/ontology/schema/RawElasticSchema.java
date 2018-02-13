package com.kayhut.fuse.executor.ontology.schema;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.List;

/**
 * Created by lior.perry on 2/11/2018.
 */
public interface RawElasticSchema {
    IndexPartitions.Impl getPartition(String type);

    String getIdFormat(String type);

    List<IndexPartitions.Partition> getPartitions(String type);

    Iterable<String> indices();


}
