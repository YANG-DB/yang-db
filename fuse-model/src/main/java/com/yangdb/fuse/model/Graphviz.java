package com.yangdb.fuse.model;


import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.EProp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

// use the awesome http://mdaines.github.io/viz.js/ to visualize the ASG plan
public abstract class Graphviz {

    private static final int NODE_LABEL_INDENT = 12;
    private static final int CLUSTER_INDENT = 2;
    private static final int INDENT = 1;


    public static String dot(String name, AsgEBase<?> root) {
        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { "
                + "rankdir=BT; \n"
                + "label=\"" + name + "\"; \n"
                + "AsgEBase[shape=plaintext, color=azure1];\n "
                + "edge[color=black,arrowsize=0.5];\n");
        handleNode(sb, root, new AtomicInteger(0), INDENT, true);
        sb.append("}");
        return sb.toString();
    }

    public static String dot(Map<String, ? extends AsgEBase<?>> clusters, boolean drawSubTrees) {
        AtomicInteger nodeCounter = new AtomicInteger(0);

        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { "
                + "rankdir=BT;\n "
                + "AsgEBase[shape=plaintext, color=azure1];\n "
                + "edge[color=black];\n "
                + "graph[compound=true];\n\n");


        int clusterNodeStart = 1;
        int clusterId = 0;

        StringBuilder clusterEdges = new StringBuilder();

        for (Entry<String, ? extends AsgEBase<?>> entry : clusters.entrySet()) {
            indent(sb, INDENT);
            // draw cluster
            sb.append("subgraph cluster");
            sb.append(++clusterId);
            sb.append(" {\n");
            indent(sb, CLUSTER_INDENT);
            sb.append("color=blue;\n");
            indent(sb, CLUSTER_INDENT);
            sb.append("label=");
            sb.append(quoteGraphviz(entry.getKey()));
            sb.append(";\n\n");

            /* to help align the clusters, add an invisible AsgEBase (that could
             * otherwise be used for labeling but it consumes too much space)
             * used for alignment */
            indent(sb, CLUSTER_INDENT);
            sb.append("c" + clusterId);
            sb.append("[style=invis]\n");
            // add edge to the first AsgEBase in the cluster
            indent(sb, CLUSTER_INDENT);
            sb.append("AsgEBase" + (nodeCounter.get() + 1));
            sb.append(" -> ");
            sb.append("c" + clusterId);
            sb.append(" [style=invis];\n");

            handleNode(sb, entry.getValue(), nodeCounter, CLUSTER_INDENT, drawSubTrees);

            int clusterNodeStop = nodeCounter.get();

            indent(sb, INDENT);
            sb.append("}\n");

            // connect cluster only if there are at least two
            if (clusterId > 1) {
                indent(clusterEdges, INDENT);
                clusterEdges.append("AsgEBase" + clusterNodeStart);
                clusterEdges.append(" -> ");
                clusterEdges.append("AsgEBase" + clusterNodeStop);
                clusterEdges.append("[ltail=cluster");
                clusterEdges.append(clusterId - 1);
                clusterEdges.append(" lhead=cluster");
                clusterEdges.append(clusterId);
                clusterEdges.append("];\n");
            }
            clusterNodeStart = clusterNodeStop;
        }

        sb.append("\n");

        // connecting the clusters arranges them in a weird position
        // so don't
        //sb.append(clusterEdges.toString());

        // align the cluster by requiring the invisible nodes in each cluster to be of the same rank
        indent(sb, INDENT);
        sb.append("{ rank=same");
        for (int i = 1; i <= clusterId; i++) {
            sb.append(" c" + i);
        }
        sb.append(" };\n}");

        return sb.toString();
    }

    private static void handleNode(StringBuilder output, AsgEBase<? extends EBase> n, AtomicInteger nodeId, int currentIndent, boolean drawSubTrees) {
        // each AsgEBase has its own id
        int thisId = nodeId.incrementAndGet();

        // first determine AsgEBase info
        StringBuilder nodeInfo = new StringBuilder();
        nodeInfo.append("\n");
        indent(nodeInfo, currentIndent + NODE_LABEL_INDENT);
        nodeInfo.append("<table border=\"0\" cellborder=\"1\" cellspacing=\"0\">\n");
        indent(nodeInfo, currentIndent + NODE_LABEL_INDENT);
        nodeInfo.append("<th><td border=\"0\" colspan=\"2\" align=\"center\"><b>"
                + n.geteBase().toString()
                + "</b></td></th>\n");
        indent(nodeInfo, currentIndent + NODE_LABEL_INDENT);

//        List<Object> props = n.nodeProperties();
        List<EProp> props = AsgQueryUtil.getEprops(n);
        List<String> parsed = new ArrayList<>(props.size());
        List<AsgEBase<?>> subTrees = new ArrayList<>();

        for (Object v : props) {
            // skip null values, children and location
            if (v != null && !n.getNext().contains(v)) {
                if (v instanceof Collection) {
                    Collection<?> c = (Collection<?>) v;
                        StringBuilder colS = new StringBuilder();
                        for (Object o : c) {
                            if (drawSubTrees && isAnotherTree(o)) {
                                subTrees.add((AsgEBase<? extends EBase>) o);
                            }
                            else {
                                colS.append(o);
                                colS.append("\n");
                            }
                        }
                        if (colS.length() > 0) {
                            parsed.add(colS.toString());
                        }
                }
                else {
                    if (drawSubTrees && isAnotherTree(v)) {
                        subTrees.add((AsgEBase<? extends EBase>) v);
                    }
                    else {
                        parsed.add(v.toString());
                    }
                }
            }
        }

        for (String line : parsed) {
            nodeInfo.append("<tr><td align=\"left\" bgcolor=\"azure2\">");
            nodeInfo.append(escapeHtml(line));
            nodeInfo.append("</td></tr>\n");
            indent(nodeInfo, currentIndent + NODE_LABEL_INDENT);
        }

        nodeInfo.append("</table>\n");

        // check any subtrees
        if (!subTrees.isEmpty()) {
            // write nested trees
            output.append("subgraph cluster_" + thisId + " {");
            output.append("style=filled; color=white; fillcolor=azure2; label=\"\";\n");
        }

        // write AsgEBase info
        indent(output, currentIndent);
        output.append("AsgEBase");
        output.append(thisId);
        output.append("[label=");
        output.append(quoteGraphviz(nodeInfo.toString()));
        output.append("];\n");

        if (!subTrees.isEmpty()) {
            indent(output, currentIndent + INDENT);
            output.append("AsgEBase[shape=ellipse, color=black]\n");

            for (AsgEBase<? extends EBase> AsgEBase : subTrees) {
                indent(output, currentIndent + INDENT);
                drawNodeTree(output, AsgEBase, "st_" + thisId + "_", 0);
            }

            output.append("\n}\n");
        }

        indent(output, currentIndent + 1);
        //output.append("{ rankdir=LR; rank=same; \n");
        int prevId = -1;
        // handle children
        for (AsgEBase<? extends EBase> c : n.getNext()) {
            // the child will always have the next id
            int childId = nodeId.get() + 1;
            handleNode(output, c, nodeId, currentIndent + INDENT, drawSubTrees);
            indent(output, currentIndent + 1);
            output.append("AsgEBase");
            output.append(childId);
            output.append(" -> ");
            output.append("AsgEBase");
            output.append(thisId);
            output.append(";\n");

            // add invisible connection between children for ordering
            if (prevId != -1) {
                indent(output, currentIndent + 1);
                output.append("AsgEBase");
                output.append(prevId);
                output.append(" -> ");
                output.append("AsgEBase");
                output.append(childId);
                output.append(";\n");
            }
            prevId = childId;
        }
        indent(output, currentIndent);
        //output.append("}\n");
    }

    private static void drawNodeTree(StringBuilder sb, AsgEBase<? extends EBase> AsgEBase, String prefix, int counter) {
        String nodeName = prefix + counter;
        prefix = nodeName;

        // draw AsgEBase
        drawNode(sb, AsgEBase, nodeName);
        // then draw all children nodes and connections between them to be on the same level
        sb.append("{ rankdir=LR; rank=same;\n");
        int prevId = -1;
        int saveId = counter;
        for (AsgEBase<? extends EBase> child : AsgEBase.getNext()) {
            int currId = ++counter;
            drawNode(sb, child, prefix + currId);
            if (prevId > -1) {
                sb.append(prefix + prevId + " -> " + prefix + currId + " [style=invis];\n");
            }
            prevId = currId;
        }
        sb.append("}\n");

        // now draw connections to the parent
        for (int i = saveId; i < counter; i++) {
            sb.append(prefix + (i + 1) + " -> " + nodeName + ";\n");
        }

        // draw the child
        counter = saveId;
        for (AsgEBase<? extends EBase> child : AsgEBase.getNext()) {
            drawNodeTree(sb, child, prefix, ++counter);
        }
    }

    private static void drawNode(StringBuilder sb, AsgEBase<? extends EBase> AsgEBase, String nodeName) {
        if (AsgEBase.getNext().isEmpty()) {
            sb.append(nodeName + " [label=\"" + AsgEBase.toString() + "\"];\n");
        }
        else {
            sb.append(nodeName + " [label=\"" + AsgEBase.geteBase().toString() + "\"];\n");
        }
    }

    private static boolean isAnotherTree(Object value) {
        if (value instanceof AsgEBase) {
            AsgEBase<? extends EBase> n = (AsgEBase<? extends EBase>) value;
            // create a subgraph
            if (n.getNext().size() > 0) {
                return true;
            }
        }
        return false;
    }

    private static String escapeHtml(Object value) {
        return String.valueOf(value)
                .replace("&", "&#38;")
                .replace("\"", "&#34;")
                .replace("'", "&#39;")
                .replace("<", "&#60;")
                .replace(">", "&#62;")
                .replace("\n", "<br align=\"left\"/>");
    }

    private static String quoteGraphviz(String value) {
        if (value.contains("<")) {
            return "<" + value + ">";
        }

        return "\"" + value + "\"";
    }

    private static String escapeGraphviz(String value) {
        return value
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace("\"", "\\\"");
    }

    private static void indent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
    }
}
