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

import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.ContentResponse;

import java.util.List;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface CatalogController {

    /**
     * get ontology resource by id
     * @param id
     * @return
     */
    ContentResponse<Ontology> getOntology(String id);

    /**
     * create new ontology
     * @param ontology
     * @return
     */
    ContentResponse<Ontology> addOntology(Ontology ontology);

    /**
     * get all ontologies
     * @return
     */
    ContentResponse<List<Ontology>> getOntologies();

    /**
     * get the physical schema by id
     * @param id
     * @return
     */
    ContentResponse<String> getSchema(String id);

    /**
     * get all the physical schemas
     * @return
     */
    ContentResponse<List<String>> getSchemas();
}
