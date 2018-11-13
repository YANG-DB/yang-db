package com.kayhut.fuse.services.controller;

/*-
 * #%L
 * fuse-domain-knowledge-poc
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

import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.Tuple2;
import javaslang.collection.Array;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kayhut.fuse.graph.algorithm.PageRank.DEFAULT_RANK_ATTRIBUTE;
import static com.kayhut.fuse.graph.view.AssignmentToGraph.enrich;
import static javafx.scene.paint.Color.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

public class GraphBuilder {
    public static Map<String, Object> getAttributes(Element node) {
        return node.getAttributeKeySet().stream().map(key -> new Tuple2<>(key, node.getAttribute(key)))
                .collect(Collectors.toMap(p -> p._1, p -> p._2));
    }

    public static Graph cloneGraph(ObjectMapper mapper, Graph g, Predicate<Node> filter, int takeTopN) {
        Graph graph = new MultiGraph(g.getId() + ".Filtered");
        g.getNodeSet().stream().filter(filter).sorted((o1, o2) ->
                (int) (Math.pow(Long.valueOf(o1.getAttribute("nodeCount").toString()), 2)
                        * (Double.valueOf(o2.getAttribute(DEFAULT_RANK_ATTRIBUTE).toString()) - Double.valueOf(o1.getAttribute(DEFAULT_RANK_ATTRIBUTE).toString()))))
                .limit(takeTopN > 0 ? takeTopN : Integer.MAX_VALUE)
                .forEach(node -> enrich(mapper, node, cloneNode(graph.addNode(node.getId()), node)));
        return graph;
    }

    private static Node cloneNode(Node target, Node source) {
        getAttributes(source).forEach((key, value) -> target.setAttribute(key, value));
        return target;
    }

    public static void populate(Graph g, Client client, Optional<String> styleSheet, Optional<String> context, List<String> fields, String... indices) {
        try {
            styleSheet.ifPresent(style -> g.setAttribute("ui.stylesheet", style));
            g.setAttribute("ui.antialias");
            g.setAttribute("ui.quality");
            final BoolQueryBuilder boolQueryBuilder = boolQuery();
            QueryBuilder qb = boolQuery().filter(
                    boolQueryBuilder
                            .mustNot(boolQuery()
                                    .should(existsQuery("deleteUser"))
                                    .should(termQuery("direction", "out"))
                            ));
            context.ifPresent(ctx->boolQueryBuilder.must(termsQuery("context", arrayOf("global", context))));

            SearchResponse scrollResp = client.prepareSearch(indices)
                    .setFetchSource(fields.toArray(new String[fields.size()]), null)
                    .setScroll(new TimeValue(60000))
                    .setQuery(qb)
                    .setSize(1000).get(); //max of 100 hits will be returned for each scroll
//Scroll until no hits are returned
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    final String id = hit.getId();
                    final String type = hit.getSource().get("type").toString();
                    Node node;
                    switch (type) {
                        case "entity":
                            node = g.getNode(id);
                            if (node == null) {
                                node = g.addNode(id);
                            }
                            node.setAttribute("ui.color", BLUE);
                            node.setAttribute("ui.size", 50f);
                            node.setAttribute("type", type);
                            node.setAttribute("context", hit.getSource().get("context").toString());
                            node.setAttribute("category", hit.getSource().get("category").toString());
                            node.setAttribute("logicalId", hit.getSource().get("logicalId").toString());
                            if (hit.getSource().containsKey("refs")) {
                                ((List) hit.getSource().get("refs")).forEach(ref -> {
                                    if (g.getNode(ref.toString()) == null) {
                                        final Node reference = g.addNode(ref.toString());
                                        reference.setAttribute("referrers", Array.of(id));
                                        reference.setAttribute("ui.color", BURLYWOOD);
                                        reference.setAttribute("ui.size", 20f);
                                        reference.setAttribute("type", "reference");
                                    } else {
                                        //append entityId to logicalId
                                        ((Array) g.getNode(ref.toString()).getAttribute("referrers")).append(id);
                                    }
                                    if (g.getEdge(id + "->" + ref.toString()) == null) {
                                        final Edge edge = g.addEdge(id + "->" + ref.toString(), id, ref.toString());
                                        edge.setAttribute("ui.color", LIGHTCORAL);
                                    }
                                });
                            }
                            break;
                        case "e.value":
                            node = g.getNode(id);
                            if (node == null) {
                                node = g.addNode(id);
                            }
                            node.setAttribute("type", type);
                            node.setAttribute("context", hit.getSource().get("context").toString());
                            String entityId = hit.getSource().get("entityId").toString();
                            String value = "";
                            if (hit.getSource().containsKey("stringValue")) {
                                value = hit.getSource().get("stringValue").toString();
                            } else if (hit.getSource().containsKey("dateValue")) {
                                value = hit.getSource().get("dateValue").toString();
                            } else if (hit.getSource().containsKey("intValue")) {
                                value = hit.getSource().get("intValue").toString();
                            }

                            node.setAttribute("fieldId", hit.getSource().get("fieldId").toString());
                            node.setAttribute("value", value);
                            node.setAttribute("ui.color", LIGHTBLUE);
                            node.setAttribute("ui.size", 30f);
                            node.setAttribute("entityId", entityId);
                            node.setAttribute("logicalId", hit.getSource().get("logicalId").toString());
                            if (hit.getSource().containsKey("refs")) {
                                ((List) hit.getSource().get("refs")).forEach(ref -> {
                                    if (g.getNode(ref.toString()) == null) {
                                        final Node reference = g.addNode(ref.toString());
                                        reference.setAttribute("context", hit.getSource().get("context").toString());
                                        reference.setAttribute("referrers", Array.of(id));
                                        reference.setAttribute("ui.color", BURLYWOOD);
                                        reference.setAttribute("ui.size", 20f);
                                        reference.setAttribute("type", "reference");
                                    } else {
                                        //append entityId to logicalId
                                        ((Array) g.getNode(ref.toString()).getAttribute("referrers")).append(id);
                                    }
                                    if (g.getEdge(id + "->" + ref.toString()) == null) {
                                        final Edge edge = g.addEdge(id + "->" + ref.toString(), id, ref.toString());
                                        edge.setAttribute("ui.color", LIGHTCORAL);
                                    }
                                });
                            }

                            //add entityId node (if not already exists)
                            if (g.getNode(entityId) == null) {
                                g.addNode(entityId);
                            }
                            if (g.getEdge(id + "->" + entityId) == null) {
                                Edge edge = g.addEdge(id + "->" + entityId, id, entityId);
                                edge.setAttribute("ui.color", LIGHTBLUE);
                            }
                            break;
                        case "e.insight":
                            node = g.getNode(id);
                            if (node == null) {
                                node = g.addNode(id);
                            }
                            node.setAttribute("ui.color", LIGHTGREEN);
                            node.setAttribute("ui.size", 30f);
                            node.setAttribute("context", hit.getSource().getOrDefault("context", "").toString());
                            node.setAttribute("type", type);
                            entityId = hit.getSource().get("entityId").toString();
                            node.setAttribute("entityId", entityId);

                            //add entityId node (if not already exists)
                            if (g.getNode(entityId) == null)
                                g.addNode(entityId);

                            if (g.getEdge(id + "->" + entityId) == null) {
                                Edge edge = g.addEdge(id + "->" + entityId, id, entityId);
                                edge.setAttribute("ui.color", LIGHTGREEN);
                            }
                            break;
                        case "e.relation":
                            String entityAId = hit.getSource().get("entityAId").toString();
                            String entityBId = hit.getSource().get("entityBId").toString();
                            //add entityId node (if not already exists)
                            if (g.getNode(entityAId) == null)
                                g.addNode(entityAId);
                            if (g.getNode(entityBId) == null)
                                g.addNode(entityBId);
                            if (g.getEdge(entityAId + "->" + entityBId) == null) {
                                Edge edge = g.addEdge(entityAId + "->" + entityBId, entityAId, entityBId);
                                edge.setAttribute("ui.color", LIGHTGOLDENRODYELLOW);
                            }
                            break;
                    }
                }

                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            }
            while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static List<String> arrayOf(String global, Optional<String> context) {
        List<String> array = new ArrayList<>();
        array.add(global);
        context.ifPresent(array::add);
        return array;
    }
}
