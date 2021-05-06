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

import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.transport.ContentResponse;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface SchemaController {

    /**
     * init mapping and indices according to the given ontology name
     * @param ontology
     * @return
     */
    ContentResponse<String> init(String ontology);

    /**
     * create mapping according to the given ontologyName name
     * @param ontologyName
     * @return
     */
    ContentResponse<String> createMapping(String ontologyName);

    /**
     * create mapping by the given instruction with the index provider strategies
     * @param ontologyName
     * @param indexProvider
     * @return
     */
    ContentResponse<String> createMapping(String ontologyName, IndexProvider indexProvider);

    /**
     * create the indices by the ontology name and index provider with the same name
     * @param ontologyName
     * @return
     */
    ContentResponse<String> createIndices(String ontologyName);

    /**
     * create the indices by the ontology name and the given index provider with the same name
     * @param ontologyName
     * @param indexProvider
     * @return
     */
    ContentResponse<String> createIndices(String ontologyName, IndexProvider indexProvider);

    /**
     * drop all indices and mapping templates
     * @param ontologyName
     * @return
     */
    ContentResponse<String> drop(String ontologyName);

}
