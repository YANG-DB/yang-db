package com.yangdb.fuse.services.engine2.data.schema;

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.typesafe.config.Config;

import java.io.File;
import java.io.IOException;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class InitialTestDataLoader implements GraphDataLoader {

    public InitialTestDataLoader() {
    }

    @Inject
    public InitialTestDataLoader(Config config, RawSchema schema) {
    }

    @Override
    public long init() throws IOException {
        return 0;
    }

    @Override
    public LoadResponse load(LogicalGraphModel root, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse load(File data, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public long drop() throws IOException {
        return 0;
    }
}
