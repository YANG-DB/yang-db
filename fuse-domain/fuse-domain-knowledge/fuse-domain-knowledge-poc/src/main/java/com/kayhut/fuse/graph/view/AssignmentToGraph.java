package com.kayhut.fuse.graph.view;

/*-
 * #%L
 * fuse-domain-knowledge-poc
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Assignment;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.kayhut.fuse.services.controller.GraphBuilder.getAttributes;
import static javafx.scene.paint.Color.*;

public class AssignmentToGraph {
    public static String styleSheet =
            "graph {"
                    + "	canvas-color: white; "
                    + "	fill-mode: gradient-radial; "
                    + "	fill-color: white, #EEEEEE; "
                    + "	padding: 80px; "
                    + "}" +
                    "node { " +
                    "   fill-mode: dyn-plain;" +
                    "   stroke-mode: plain;" +
                    "	size-mode: dyn-size;" +
                    "   text-visibility-mode: zoom-range;" +
                    "   text-visibility: 0, 0.9;" +
                    "} " +
                    "node.Entity { " +
                    "   fill-mode: dyn-plain;" +
                    "   text-style: bold;" +
                    "	shape: rounded-box;" +
                    "	stroke-mode: plain;" +
                    "} " +
                    "node.Value { " +
                    "	text-background-mode: rounded-box;" +
                    "	text-background-color: #D2B48C;" +
                    "} " +
                    "node.Reference { " +
                    "	shape: circle;" +
                    "	text-background-mode: rounded-box;" +
                    "	text-background-color: #D6B48C;" +
                    "	text-alignment: above;" +
                    "} " +
                    "node.Insight { " +
                    "	shape: diamond;" +
                    "	text-background-mode: rounded-box;" +
                    "	text-background-color: #E6B48C;" +
                    "	text-alignment: above;" +
                    "} " +
                    "node:clicked {" +
                    "   stroke-mode: plain;" +
                    "   stroke-color: red;" +
                    "}" +
                    "node:selected {" +
                    "   stroke-mode: plain;" +
                    "   stroke-width: 6px;" +
                    "   stroke-color: blue;" +
                    "}" +
                    "edge {" +
                    "    fill-mode: dyn-plain; " +
                    "	 size-mode: dyn-size;" +
                    "}" +
                    "edge.Insight {" +
                    "    shape: cubic-curve;" +
                    "    fill-mode: dyn-plain; " +
                    "	 size-mode: dyn-size;" +
                    "}" +
                    "edge.Reference {" +
                    "    shape: cubic-curve;" +
                    "    fill-mode: dyn-plain; " +
                    "	 size-mode: dyn-size;" +
                    "}" +
                    "edge.Relation {" +
                    "    shape: cubic-curve;" +
                    "    size: 3;" +
                    "    fill-mode: dyn-plain; " +
                    "	 size-mode: dyn-size;" +
                    "}";


    public static Node enrich(ObjectMapper mapper, Node source, Node target) {
        switch (source.getAttribute("type").toString()) {
            case "entity":
                List<ObjectNode> collect = iteratorToStream(source.getNeighborNodeIterator(), false)
                        .filter(p -> p.getAttribute("type").equals("e.value"))
                        .map(q -> {
                            target.setAttribute("value."+q.getAttribute("fieldId"),q.getAttribute("value").toString());
                            return mapper.createObjectNode().put(q.getAttribute("fieldId").toString(), q.getAttribute("value").toString());
                        })
                        .collect(Collectors.toList());
                target.setAttribute("values", mapper.createArrayNode().addAll(collect));
                target.setAttribute("label", source.getAttribute("category").toString());
                //populate with entity global's values
                final String logicalId = source.getAttribute("logicalId") + ".global";
                if (!source.getAttribute("context").equals("global") &&
                        source.getGraph().getNode(logicalId) != null) {
                    final Node globalEntity = source.getGraph().getNode(logicalId);
                    collect = iteratorToStream(globalEntity.getNeighborNodeIterator(), false)
                            .filter(p -> p.getAttribute("type").equals("e.value"))
                            .map(q -> {
                                target.setAttribute("value."+q.getAttribute("fieldId"),q.getAttribute("value").toString());
                                return mapper.createObjectNode().put(q.getAttribute("fieldId").toString(), q.getAttribute("value").toString());
                            })
                            .collect(Collectors.toList());
                    ((ArrayNode) target.getAttribute("values")).addAll(collect);
                }
                break;
            case "e.value":
                break;
            case "insight":
                break;
            case "reference":
                break;
        }
        return target;
    }

    public static Graph populateFromAssignment(ObjectMapper mapper, Graph g, Assignment assignment) {
        Graph subGraph = new MultiGraph(assignment.toString());
        if (assignment.getEntities() != null) {
            assignment.getEntities().forEach(n -> {
                if (g.getNode(n.geteID()) != null) {
                    Node node = subGraph.addNode(n.geteID());
                    getAttributes(g.getNode(n.geteID())).forEach(node::setAttribute);
                    switch (n.geteType()) {
                        case "Entity":
                            final String category = n.getProperties().stream().filter(v -> v.getpType().equals("category"))
                                    .findAny().get().getValue().toString();
                            final List<ObjectNode> collect = iteratorToStream(g.getNode(n.geteID()).getNeighborNodeIterator(), false)
                                    .filter(p -> p.getAttribute("type").equals("e.value"))
                                    .map(q -> mapper.createObjectNode().put(q.getAttribute("fieldId").toString(), q.getAttribute("value").toString()))
                                    .collect(Collectors.toList());
                            node.setAttribute("values", mapper.createArrayNode().addAll(collect));
                            node.setAttribute("label", category);
                            node.setAttribute("ui.label", category);
                            node.setAttribute("ui.color", NamedColors.get(n.geteType() + category));
                            node.setAttribute("ui.size", 20f);
                            node.setAttribute("ui.class", "Entity");
                            node.setAttribute("ui.properties", n.getProperties());
                            break;
                        case "Evalue":
                            String fieldId = n.getProperties().stream().filter(v -> v.getpType().equals("fieldId"))
                                    .findAny().get().getValue().toString();
                            String value = n.getProperties().stream().filter(v ->
                                    v.getpType().equals("stringValue") ||
                                            v.getpType().equals("intValue") ||
                                            v.getpType().equals("longValue") ||
                                            v.getpType().equals("floatValue") ||
                                            v.getpType().equals("dateValue"))
                                    .findAny().get().getValue().toString();

                            value = value.length() > 10 ? value.substring(0, Math.min(value.length(), 10)) + "..." : value;

                            node.setAttribute("ui.color", NamedColors.get(n.geteType()));
                            node.setAttribute("label", fieldId + ":" + value);
                            node.setAttribute("ui.label", fieldId + ":" + value);
                            node.setAttribute("ui.value", fieldId + ":" + value);
                            node.setAttribute("ui.class", "Value");
                            node.setAttribute("ui.properties", n.getProperties());
                            break;
                        case "Insight":
                            String content = n.getProperties().stream().filter(v -> v.getpType().equals("content"))
                                    .findAny().get().getValue().toString();
                            String shortContent = content.length() > 10 ? content.substring(0, Math.min(content.length(), 10)) + "..." : content;

                            node.setAttribute("ui.color", NamedColors.get(n.geteType()));
                            node.setAttribute("label", shortContent);
                            node.setAttribute("ui.label", shortContent);
                            node.setAttribute("ui.value", content);
                            node.setAttribute("ui.class", "Insight");
                            node.setAttribute("ui.properties", n.getProperties());
                            break;
                        case "Reference":
                            String title = n.getProperties().stream().filter(v -> v.getpType().equals("title"))
                                    .findAny().get().getValue().toString();
                            String shortTitle = title.length() > 10 ? title.substring(0, Math.min(title.length(), 10)) + "..." : title;
                            node.setAttribute("ui.color", NamedColors.get(n.geteType()));
                            node.setAttribute("label", shortTitle);
                            node.setAttribute("ui.label", shortTitle);
                            node.setAttribute("ui.value", title);
                            node.setAttribute("ui.class", "Reference");
                            node.setAttribute("ui.properties", n.getProperties());
                            break;
                        case "Rvalue":
                            break;
                        case "Relation":
                            final String edgeCategory = n.getProperties().stream().filter(v -> v.getpType().equals("category"))
                                    .findAny().get().getValue().toString();

                            final String sideA = n.getProperties().stream().filter(v -> v.getpType().equals("entityAId"))
                                    .findAny().get().getValue().toString();
                            final String sideB = n.getProperties().stream().filter(v -> v.getpType().equals("entityBId"))
                                    .findAny().get().getValue().toString();
                            //assuming sideA is present in the results - if side B not present -> add node to represent it

                            final String sideBCategory = n.getProperties().stream().filter(v -> v.getpType().equals("entityBCategory"))
                                    .findAny().get().getValue().toString();

                            Node nodeSideB = subGraph.addNode(sideB);
                            getAttributes(g.getNode(sideB)).forEach(nodeSideB::setAttribute);

                            nodeSideB.setAttribute("label", sideBCategory);
                            nodeSideB.setAttribute("ui.label", sideBCategory);
                            nodeSideB.setAttribute("ui.color", NamedColors.get(n.geteType() + sideBCategory));
                            nodeSideB.setAttribute("ui.size", 20f);
                            nodeSideB.setAttribute("ui.class", "Entity");


                            final Edge edge = subGraph.addEdge(sideA + "->" + sideB, sideA, sideB);
                            getAttributes(g.getEdge(sideA + "->" + sideB)).forEach(edge::setAttribute);

                            edge.setAttribute("ui.color", BLUE);
                            edge.setAttribute("ui.label", edgeCategory);
                            edge.setAttribute("ui.class", "Relation");
                            edge.setAttribute("ui.properties", n.getProperties());
                            break;
                    }
                }
            });
        }
        if (assignment.getRelationships() != null) {
            assignment.getRelationships().forEach(r -> {
                if (g.getEdge(r.geteID1() + "->" + r.geteID2()) != null) {
                    final Edge edge = subGraph.addEdge(r.getrID(), r.geteID1(), r.geteID2());
                    edge.setAttribute("ui.label", r.getrType());
                    edge.setAttribute("ui.class", "Relation");
                    edge.setAttribute("ui.properties", r.getProperties());
                }
            });
        }
        return subGraph;
    }

    public static <T> Stream<T> iteratorToStream(final Iterator<T> iterator, final boolean parallell) {
        return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), parallell);
    }

    public static void populateGraph(Graph g, Assignment assignment) {
        final String[] context = new String[1];
        if (assignment.getEntities() != null) {
            assignment.getEntities().forEach(n -> {
                if (g.getNode(n.geteID()) == null) {
                    if (n.getProperties().stream().anyMatch(v -> v.getpType().equals("context"))) {
                        context[0] = n.getProperties().stream().filter(v -> v.getpType().equals("context")).findAny().get().getValue().toString();
                        if (g.getNode(context[0]) == null) {
                            final Node node = g.addNode(context[0]);
                            node.setAttribute("ui.label", context[0]);
                            node.setAttribute("label", context[0]);
                            node.setAttribute("ui.color", BEIGE);
                            node.setAttribute("ui.size", 50f);
                        }
                    }
                    Node node;

                    switch (n.geteType()) {
                        case "Entity":
                            final String category = n.getProperties().stream().filter(v -> v.getpType().equals("category"))
                                    .findAny().get().getValue().toString();
                            node = g.addNode(n.geteID());
                            node.setAttribute("label", category);
                            node.setAttribute("ui.label", category);
                            node.setAttribute("ui.color", NamedColors.get(n.geteType() + category));
                            node.setAttribute("ui.size", 20f);
                            node.setAttribute("ui.class", "Entity");
                            node.setAttribute("ui.properties", n.getProperties());

                            if (context[0] != null) {
/*
                        final Edge edge = g.addEdge(context[0] + "->" + n.geteID(), context[0], n.geteID());
                        edge.setAttribute("ui.size", 5f);
                        edge.setAttribute("ui.color", GRAY);
*/
                                break;
                            }
                        case "Evalue":
                            String fieldId = n.getProperties().stream().filter(v -> v.getpType().equals("fieldId"))
                                    .findAny().get().getValue().toString();
                            String value = n.getProperties().stream().filter(v ->
                                    v.getpType().equals("stringValue") ||
                                            v.getpType().equals("intValue") ||
                                            v.getpType().equals("longValue") ||
                                            v.getpType().equals("floatValue") ||
                                            v.getpType().equals("dateValue"))
                                    .findAny().get().getValue().toString();

                            value = value.length() > 10 ? value.substring(0, Math.min(value.length(), 10)) + "..." : value;


                            node = g.addNode(n.geteID());
                            node.setAttribute("ui.color", NamedColors.get(n.geteType()));
                            node.setAttribute("label", fieldId + ":" + value);
                            node.setAttribute("ui.label", fieldId + ":" + value);
                            node.setAttribute("ui.value", fieldId + ":" + value);
                            node.setAttribute("ui.class", "Value");
                            node.setAttribute("ui.properties", n.getProperties());

                            break;
                        case "Insight":
                            String content = n.getProperties().stream().filter(v -> v.getpType().equals("content"))
                                    .findAny().get().getValue().toString();
                            String shortContent = content.length() > 10 ? content.substring(0, Math.min(content.length(), 10)) + "..." : content;

                            node = g.addNode(n.geteID());
                            node.setAttribute("ui.color", NamedColors.get(n.geteType()));
                            node.setAttribute("label", shortContent);
                            node.setAttribute("ui.label", shortContent);
                            node.setAttribute("ui.value", content);
                            node.setAttribute("ui.class", "Insight");
                            node.setAttribute("ui.properties", n.getProperties());
                            break;
                        case "Reference":
                            String title = n.getProperties().stream().filter(v -> v.getpType().equals("title"))
                                    .findAny().get().getValue().toString();
                            String shortTitle = title.length() > 10 ? title.substring(0, Math.min(title.length(), 10)) + "..." : title;
                            node = g.addNode(n.geteID());
                            node.setAttribute("ui.color", NamedColors.get(n.geteType()));
                            node.setAttribute("label", shortTitle);
                            node.setAttribute("ui.label", shortTitle);
                            node.setAttribute("ui.value", title);
                            node.setAttribute("ui.class", "Reference");
                            node.setAttribute("ui.properties", n.getProperties());
                            break;
                        case "Rvalue":
                            break;
                        case "Relation":
                            final String edgeCategory = n.getProperties().stream().filter(v -> v.getpType().equals("category"))
                                    .findAny().get().getValue().toString();

                            final String sideA = n.getProperties().stream().filter(v -> v.getpType().equals("entityAId"))
                                    .findAny().get().getValue().toString();
                            final String sideB = n.getProperties().stream().filter(v -> v.getpType().equals("entityBId"))
                                    .findAny().get().getValue().toString();
                            //assuming sideA is present in the results - if side B not present -> add node to represent it
                            if (g.getNode(sideB) == null) {
                                final String sideBCategory = n.getProperties().stream().filter(v -> v.getpType().equals("entityBCategory"))
                                        .findAny().get().getValue().toString();
                                Node nodeSideB = g.addNode(sideB);
                                nodeSideB.setAttribute("label", sideBCategory);
                                nodeSideB.setAttribute("ui.label", sideBCategory);
                                nodeSideB.setAttribute("ui.color", NamedColors.get(n.geteType() + sideBCategory));
                                nodeSideB.setAttribute("ui.size", 20f);
                                nodeSideB.setAttribute("ui.class", "Entity");
                            }
                            if (g.getEdge(n.geteID()) == null) {
                                Edge in = g.addEdge(n.geteID(), sideA, sideB, false);
                                in.setAttribute("ui.color", BLUE);
                                in.setAttribute("ui.label", edgeCategory);
                                in.setAttribute("ui.class", "Relation");
                                in.setAttribute("ui.properties", n.getProperties());
                            }
                            break;
                    }
                }
            });
        }
        if (assignment.getRelationships() != null) {
            assignment.getRelationships().forEach(r -> {
                if (g.getEdge(r.getrID()) == null) {
                    switch (r.getrType()) {
                        case "hasEvalue":
                            final Edge eval = g.addEdge(r.getrID(), r.geteID1(), r.geteID2(), false);
                            eval.setAttribute("ui.size", 1f);
                            eval.setAttribute("ui.color", GRAY);
                            break;
                        case "hasInsight":
                            final Edge insight = g.addEdge(r.getrID(), r.geteID1(), r.geteID2(), false);
                            insight.setAttribute("ui.color", BLUEVIOLET);
                            insight.setAttribute("ui.class", "Insight");
                            break;
                        case "hasEntityReference":
                            final Edge reference = g.addEdge(r.getrID(), r.geteID1(), r.geteID2(), false);
                            reference.setAttribute("ui.color", GREEN);
                            reference.setAttribute("ui.class", "Reference");
                            break;
                        case "hasRelationReference":
                            break;
                        case "hasRvalue":
                            break;
                    }
                }
            });
        }

    }
}
