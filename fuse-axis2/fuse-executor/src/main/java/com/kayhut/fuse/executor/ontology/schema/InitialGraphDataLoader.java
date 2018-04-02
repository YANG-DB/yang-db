package com.kayhut.fuse.executor.ontology.schema;

import java.io.IOException;

/**
 * Created by lior.perry on 2/11/2018.
 */
public interface InitialGraphDataLoader {
    long init() throws IOException;
    long load() throws IOException;
    long drop() throws IOException;
}
