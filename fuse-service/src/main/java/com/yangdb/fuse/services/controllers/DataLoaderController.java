package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.ContentResponse;

import java.io.File;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface DataLoaderController {

    ContentResponse<String> init(String ontology);
    ContentResponse<String> drop(String ontology);

    ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive);
    ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive);

    ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive);
    ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive);
}
