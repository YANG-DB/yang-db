package com.yangdb.fuse.executor.ontology.schema.load;

import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.io.File;
import java.io.IOException;

public interface CSVDataLoader {

    /**
     * does:
     *  - unzip file
     *  - split to multiple small files
     *  - for each file (in parallel)
     *      - convert into bulk set
     *      - commit to repository
     */
    LoadResponse<String, FuseError> load(File data, GraphDataLoader.Directive directive) throws IOException;

}
