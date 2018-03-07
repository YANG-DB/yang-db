package com.kayhut.fuse.test;

import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.schema.InitialGraphDataLoader;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.typesafe.config.Config;

import java.io.IOException;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class InitialTestDataLoader implements InitialGraphDataLoader {

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
    public long load() throws IOException {
        return 0;
    }

    @Override
    public long drop() throws IOException {
        return 0;
    }
}
