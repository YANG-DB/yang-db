package com.yangdb.fuse.services.engine2.data.schema;

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.schema.load.CSVDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.results.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.typesafe.config.Config;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.io.File;
import java.io.IOException;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class InitialTestDataLoader implements GraphDataLoader, CSVDataLoader {

    public InitialTestDataLoader() {
    }

    @Inject
    public InitialTestDataLoader(Config config, RawSchema schema) {
    }

    @Override
    public LoadResponse load(String ontology, LogicalGraphModel root, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse load(String ontology, File data, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String label, File data, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse<String, FuseError> load(String type, String label, String payload, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }
}
