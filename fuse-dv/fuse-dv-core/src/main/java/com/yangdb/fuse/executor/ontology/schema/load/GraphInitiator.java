package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import java.io.IOException;

public interface GraphInitiator {

    /**
     * create the indexTemplates
     * create the vertices and edges indices according to schema
     *
     * @return
     * @throws IOException
     * @param ontology
     */
    long init(String ontology) ;

    /**
     * create the indexTemplates
     * create the vertices and edges indices according to schema
     *
     * @return
     * @throws IOException
     */
    long init() ;

    /**
     * drop the vertices and edges indices to schema
     *
     * @return
     * @throws IOException
     * @param ontologyName
     */
    long drop(String ontologyName) ;

    /**
     * drop the vertices and edges indices to schema
     *
     * @return
     * @throws IOException
     */
    long drop() throws IOException;

    /**
     * generate the elasticsearch index template according to ontology and index schema provider json instructions
     *
     * @param ontologyName
     * @param indexProvider
     * @return
     */
    long createTemplate(String ontologyName, IndexProvider indexProvider) ;

    /**
     * generate the elasticsearch index template according to ontology and index schema provider json instructions
     *
     * @param ontologyName
     * @return
     */
    long createTemplate(String ontologyName) ;

    /**
     * create indices according to ontology and index schema provider json instructions
     * @param ontologyName
     * @param indexProvider
     * @return
     * @throws IOException
     */
    long createIndices(String ontologyName, IndexProvider indexProvider) ;

   /**
     * create indices according to ontology and index schema provider json instructions
     * @param ontologyName
     * @return
     * @throws IOException
     */
    long createIndices(String ontologyName) ;
}
