package com.kayhut.fuse.model.logical;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
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
 *                 "metadata": {
 *                     "label": "person",
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
 *     }
 * */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LogicalGraphModel {
    public static final String LABEL = "label";
    private List<LogicalNode> nodes;
    private List<LogicalEdge> edges;

    public LogicalGraphModel() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public List<LogicalNode> getNodes() {
        return nodes;
    }

    public List<LogicalEdge> getEdges() {
        return edges;
    }

}
