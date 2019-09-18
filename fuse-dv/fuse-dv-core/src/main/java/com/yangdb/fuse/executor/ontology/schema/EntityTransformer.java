package com.yangdb.fuse.executor.ontology.schema;

import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import org.elasticsearch.client.Client;

public class EntityTransformer implements DataTransformer {

    public EntityTransformer(RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
    }

    @Override
    public Object transform(LogicalGraphModel graph, GraphDataLoader.Directive directive) {
        return null;
    }


}
