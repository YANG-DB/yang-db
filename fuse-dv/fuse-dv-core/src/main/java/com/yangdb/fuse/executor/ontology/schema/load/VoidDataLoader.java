package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.schema.load.CSVDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.typesafe.config.Config;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.io.File;
import java.io.IOException;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class VoidDataLoader implements GraphDataLoader, CSVDataLoader {

    public VoidDataLoader() {}

    @Inject
    public VoidDataLoader(Config config, RawSchema schema) {
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
