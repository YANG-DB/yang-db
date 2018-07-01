package com.kayhut.fuse.executor.ontology.schema;

import java.io.IOException;

public class VoidGraphDataLoader implements InitialGraphDataLoader {
    //region InitialGraphDataLoader Implementation
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
    //endregion
}
