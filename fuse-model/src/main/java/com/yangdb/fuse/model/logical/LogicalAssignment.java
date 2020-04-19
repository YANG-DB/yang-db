package com.yangdb.fuse.model.logical;

/*-
 * #%L
 * fuse-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.model.results.AssignmentIfc;

import java.util.ArrayList;
import java.util.List;

/**
 * {
 *   "assignment": {
 *     "nodes": [
 *       {
 *         "id": "0",
 *         "label": "person",
 *         "metadata": {
 *           "user-defined": "values"
 *         },
 *         "properties": {
 *           "fName": "first name",
 *           "lName": "last name",
 *           "born": "12/12/2000",
 *           "age": "19",
 *           "email": "myName@fuse.com",
 *           "address": {
 *             "state": "my state",
 *             "street": "my street",
 *             "city": "my city",
 *             "zip": "gZip"
 *           }
 *         }
 *       },
 *       {
 *         "id": "10",
 *         "label": "person",
 *         "metadata": {
 *           "user-defined": "values"
 *         },
 *         "properties": {
 *           "fName": "another first name",
 *           "lName": "another last name",
 *           "age": "20",
 *           "born": "1/1/1999",
 *           "email": "notMyName@fuse.com",
 *           "address": {
 *             "state": "not my state",
 *             "street": "not my street",
 *             "city": "not my city",
 *             "zip": "not gZip"
 *           }
 *         }
 *       }
 *     ],
 *     "edges": [
 *       {
 *         "id": 100,
 *         "source": "0",
 *         "target": "1",
 *         "metadata": {
 *           "label": "knows",
 *           "user-defined": "values"
 *         },
 *         "properties": {
 *           "date": "01/01/2000",
 *           "medium": "facebook"
 *         }
 *       },
 *       {
 *         "id": 101,
 *         "source": "0",
 *         "target": "1",
 *         "metadata": {
 *           "label": "called",
 *           "user-defined": "values"
 *         },
 *         "properties": {
 *           "date": "01/01/2000",
 *           "duration": "120",
 *           "medium": "cellular",
 *           "sourceLocation": "40.06,-71.34",
 *           "sourceTarget": "41.12,-70.9"
 *         }
 *       }
 *     ]
 *   }
 * }
  */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalAssignment implements AssignmentIfc<LogicalNode,LogicalEdge> {

    @JsonProperty("nodes")
    private List<LogicalNode> nodes;
    @JsonProperty("edges")
    private List<LogicalEdge> edges;

    public LogicalAssignment() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    @JsonIgnore
    public LogicalAssignment with(LogicalNode node) {
        getEntities().add(node);
        return this;
    }

    @JsonIgnore
    public LogicalAssignment with(LogicalEdge edge) {
        getRelationships().add(edge);
        return this;
    }


    @Override
    @JsonProperty("edges")
    public List<LogicalEdge> getRelationships() {
        return edges;
    }

    @Override
    @JsonProperty("nodes")
    public List<LogicalNode> getEntities() {
        return nodes;
    }
}
