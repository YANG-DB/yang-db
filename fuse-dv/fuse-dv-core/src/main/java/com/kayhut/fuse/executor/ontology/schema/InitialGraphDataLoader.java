package com.kayhut.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Created by lior.perry on 2/11/2018.
 */
public interface InitialGraphDataLoader {
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
     *     "graph": {
     *         "nodes": [
     *             {
     *                 "id": "0",
     *                 "metadata": {
     *                     "type": "person",
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": {
     *                         "value":"first name"
     *                     },
     *                     "lName": {
     *                         "value":"last name"
     *                     },
     *                     "age": {
     *                         "value":"45"
     *                     },
     *                     "email": {
     *                         "value":"myName@fuse.com"
     *                     },
     *                     "address": {
     *                         "value": {
     *                             "state": "my state",
     *                             "street": "my street",
     *                             "city": "my city",
     *                             "zip": "gZip"
     *                         }
     *                     }
     *                 }
     *             },
     *             {
     *                 "id": "10",
     *                 "metadata": {
     *                     "type": "person",
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": {
     *                         "value":"another first name"
     *                     },
     *                     "lName": {
     *                         "value":"another last name"
     *                     },
     *                     "age": {
     *                         "value":"35"
     *                     },
     *                     "email": {
     *                         "value":"yourName@fuse.com"
     *                     },
     *                     "address": {
     *                         "value": {
     *                             "state": "your state",
     *                             "street": "your street",
     *                             "city": "your city",
     *                             "zip": "tar"
     *                         }
     *                     }
     *                 }
     *             }
     *         ],
     *         "edges": [
     *             {
     *                 "id": 100,
     *                 "source": "0",
     *                 "target": "1",
     *                 "directed": false,
     *                 "metadata": {
     *                     "type": "knows",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "time": {
     *                          "date":"01-01-2000"
     *                      },
     *                      "medium": {
     *                          "value":"facebook"
     *                      }
     *                 }
     *             },
     *             {
     *                 "id": 101,
     *                 "source": "0",
     *                 "target": "1",
     *                 "directed": false,
     *                 "metadata": {
     *                     "type": "called",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date": {
     *                          "date":"01-01-2000"
     *                      },
     *                      "duration": {
     *                          "value":"120s"
     *                      },
     *                      "location": {
     *                          "source": {
     *                              "lat": 41.12,
     *                              "lon": -71.34
     *                          },
     *                          "target": {
     *                              "lat": 41.2,
     *                              "lon": -70.34
     *                          }
     *
     *                      }
     *                 }
     *             }
     *         ]
     *     }
     * }
     * @param root
     * @return
     * @throws IOException
     */
    long load(JsonNode root) throws IOException;

    /**
     * drop the vertices and edges indices to schema
     * @return
     * @throws IOException
     */
    long drop() throws IOException;
}
