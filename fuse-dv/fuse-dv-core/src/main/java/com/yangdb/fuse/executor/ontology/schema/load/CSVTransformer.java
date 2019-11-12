package com.yangdb.fuse.executor.ontology.schema.load;

import com.yangdb.fuse.executor.ontology.DataTransformer;

public class CSVTransformer implements DataTransformer<DataTransformerContext, String> {

    @Override
    public DataTransformerContext transform(String data, GraphDataLoader.Directive directive) {
        return null;
    }
}
