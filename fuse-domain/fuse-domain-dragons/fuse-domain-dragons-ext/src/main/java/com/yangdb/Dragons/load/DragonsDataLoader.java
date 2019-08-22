package com.yangdb.Dragons.load;

import com.yangdb.fuse.executor.ontology.schema.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.LoadResponse;
import com.yangdb.fuse.model.logical.LogicalGraphModel;

import java.io.File;
import java.io.IOException;

public class DragonsDataLoader implements GraphDataLoader {
    @Override
    public long init() throws IOException {
        return 0;
    }

    @Override
    public LoadResponse load(LogicalGraphModel root, Directive directive) throws IOException {
        return null;
    }

    @Override
    public LoadResponse load(File data, Directive directive) throws IOException {
        return null;
    }

    @Override
    public long drop() throws IOException {
        return 0;
    }
}
