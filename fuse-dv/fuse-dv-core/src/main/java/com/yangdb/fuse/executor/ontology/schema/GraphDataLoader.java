package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.model.logical.LogicalGraphModel;

import java.io.File;
import java.io.IOException;

/**
 * Created by lior.perry on 2/11/2018.
 */
public interface GraphDataLoader<S,F> {
    /**
     * create the indexTemplates
     * create the vertices and edges indices according to schema
     * @return
     * @throws IOException
     */
    long init() throws IOException;

    /**
     * load the given input json graph - all must comply with the ontology and physical schema bounded
     *
     * Example:
     * {
     *         "nodes": [
     *             {
     *                 "id": "0",
     *                 "metadata": {
     *                     "label": "person",
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": "first name",
     *                     "lName":"last name",
     *                     "born": "12/12/2000",
     *                     "age": "19",
     *                     "email": "myName@fuse.com",
     *                     "address": {
     *                             "state": "my state",
     *                             "street": "my street",
     *                             "city": "my city",
     *                             "zip": "gZip"
     *                     }
     *                 }
     *             },
     *             {
     *                 "id": "10",
 *                     "label": "person",
     *                 "metadata": {
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": "another first name",
     *                     "lName":"another last name",
     *                     "age": "20",
     *                     "born": "1/1/1999",
     *                     "email": "notMyName@fuse.com",
     *                     "address": {
     *                             "state": "not my state",
     *                             "street": "not my street",
     *                             "city": "not my city",
     *                             "zip": "not gZip"
     *                     }
     *                 }
     *             }
     *         ],
     *         "edges": [
     *             {
     *                 "id": 100,
     *                 "source": "0",
     *                 "target": "1",
     *                 "metadata": {
     *                     "label": "knows",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date":"01/01/2000",
     *                      "medium": "facebook"
     *                 }
     *             },
     *             {
     *                 "id": 101,
     *                 "source": "0",
     *                 "target": "1",
     *                 "metadata": {
     *                     "label": "called",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date":"01/01/2000",
     *                      "duration":"120",
     *                      "medium": "cellular"
     *                      "sourceLocation": "40.06,-71.34"
     *                      "sourceTarget": "41.12,-70.9"
     *                 }
     *             }
     *         ]
     * }
     * @param root
     * @param directive
     * @return
     * @throws IOException
     */
    LoadResponse<S, F> load(LogicalGraphModel root, Directive directive) throws IOException;

    /**
     * does:
     *  - unzip file
     *  - split to multiple small files
     *  - for each file (in parallel)
     *      - convert into bulk set
     *      - commit to repository
     */
    LoadResponse<S, F> load(File data, Directive directive) throws IOException;

    /**
     * drop the vertices and edges indices to schema
     * @return
     * @throws IOException
     */
    long drop() throws IOException;

    public static enum Directive {
        INSERT,UPSERT
    }

}
