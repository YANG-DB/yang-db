package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class TestRawSchema implements RawSchema {
    @Override
    public IndexPartitions getPartition(String type) {
        return new IndexPartitions.Impl(type);
    }

    @Override
    public String getIdFormat(String type) {
        return "";
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<String> indices() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> types() {
        return Collections.EMPTY_SET;
    }
}
