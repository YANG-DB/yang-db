/*
 * This file is part of GraphStream <http://graphstream-project.org>.
 *
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 *
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */

/**
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */

package com.kayhut.fuse.view;


import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import javafx.scene.paint.Color;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.util.DefaultApplication;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.junit.Assert;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static java.lang.Thread.sleep;
import static javafx.scene.paint.Color.*;


public class FuseViewer implements ViewerListener {
    private boolean loop = true;

    public static void main(String[] args) throws Exception {
        System.setProperty("org.graphstream.ui", "org.graphstream.ui.javafx.util.Display");
        new FuseViewer().run();
//        Application.launch(FuseViewer.class);
    }

    public FuseViewer() throws IOException {
        fuseClient = new FuseClient("http://localhost:8888/fuse");
        fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        graph = new MultiGraph("g1");

    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        try {
            //continue graph exploration
            final Node node = graph.getNode(id);
            if (node.getAttribute("ui.selected") != null &&
                    node.getAttribute("ui.selected").toString().equals("true")) {
                //show label
                showId(node, true);
            } else {
                if (node.getAttribute("ui.expanded") == null) {
                    populateGraph(graph, id.split("\\.")[0]);
                    node.setAttribute("ui.expanded", "true");
                }
                showId(node, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showId(Node node, boolean show) {
        if (show) {
            node.setAttribute("label", node.getId());
        } else {
            node.setAttribute("label", node.getAttribute("ui.label").toString());
        }

    }

    public void buttonReleased(String id) {
    }

    public void mouseOver(String id) {
    }

    public void mouseLeft(String id) {
    }

    public static List<Node> filter(Graph g, Predicate<Node> predicate) {
        return g.nodes().filter(predicate).collect(Collectors.toList());
    }


    public void run() throws Exception {
        viewer = graph.display(true);

        // The default action when closing the view is to quit
        // the program.
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        viewer.enableAutoLayout();

        ViewerPipe pipeIn = viewer.newViewerPipe();
        view = (FxDefaultView) viewer.addView("view1", new FxGraphRenderer());
        view.getCamera().setViewPercent(0.75);
        view.setShortcutManager(new KeysManager(graph));
        view.setMouseManager(new MouseManager());

        DefaultApplication.init(view, graph);
        pipeIn.addAttributeSink(graph);
        pipeIn.addViewerListener(this);
        pipeIn.pump();

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.quality");

        populateGraph(graph);

        while (loop) {
            pipeIn.pump();
            sleep(100);
        }

        System.out.println("bye bye");
        System.exit(0);


    }


    protected String styleSheet =
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

    private void populateGraph(Graph g, String... values) throws IOException, InterruptedException {
        Assignment assignment = fetchAssignment(values);
        final String[] context = new String[1];
        if (assignment.getEntities() != null) {
            assignment.getEntities().forEach(n -> {
                if (g.getNode(n.geteID()) == null) {
                    if (n.getProperties().stream().anyMatch(v -> v.getpType().equals("context"))) {
                        context[0] = n.getProperties().stream().filter(v -> v.getpType().equals("context")).findAny().get().getValue().toString();
                        if (g.getNode(context[0]) == null) {
                            final Node node = g.addNode(context[0]);
                            node.setAttribute("ui.size", 50f);
                            node.setAttribute("label", context[0]);
                            node.setAttribute("ui.label", context[0]);
                            node.setAttribute("ui.color", BEIGE);
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
                            System.out.println("add ref "+n.geteID());
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
                            System.out.println("add edge "+r.geteID1() +"->" + r.geteID2());
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

    private Assignment fetchAssignment(String... values) throws IOException, InterruptedException {
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query($ont, values));
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        cursorRequest.setTimeout(50000);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 3000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                sleep(10);
            }
        }

        AssignmentsQueryResult pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertFalse(pageData.getAssignments().isEmpty());
        return pageData.getAssignments().get(0);
    }

    private Query query(Ontology.Accessor $ont, String... values) {
        ArrayList<EBase> list = new ArrayList<>();
        list.add(new Start(0, 1));
        list.add(new ETyped(1, "SE", $ont.eType$("Entity"), 2, 0));

        if (values.length > 0) {
            list.add(new Quant1(2, QuantType.all, Arrays.asList(3, 4, 12, 20, 13, 555), 0));
            list.add(new EProp(3, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")));
            list.add(new EProp(4, $ont.pType$("logicalId"), Constraint.of(ConstraintOp.inSet, Arrays.asList(values))));
        } else {
            list.add(new Quant1(2, QuantType.all, Arrays.asList(3, 12, 20, 13, 555), 0));
            list.add(new EProp(3, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")));
        }

        list.addAll(Arrays.asList(
                new Rel(20, $ont.rType$("hasEvalue"), R, null, 21, 0),
                new ETyped(21, "B", $ont.eType$("Evalue"), 0, 0),

                new OptionalComp(555, 5),
                new Rel(5, $ont.rType$("hasRelation"), R, null, 6, 0),
                new ETyped(6, "R", $ont.eType$("Relation"), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(10, 11), 0),

                new OptionalComp(12, 120),
                new Rel(120, "hasEntityReference", R, null, 121, 0),
                new ETyped(121, "ERef", "Reference", 0, 0),

                new OptionalComp(13, 130),
                new Rel(130, "hasInsight", R, null, 131, 0),
                new ETyped(131, "Ins", "Insight", 0, 0),

                new OptionalComp(10, 100),
                new Rel(100, "hasRvalue", R, null, 101, 0),
                new ETyped(101, "RV", "Rvalue", 0, 0),

                new OptionalComp(11, 110),
                new Rel(110, "hasRelationReference", R, null, 111, 0),
                new ETyped(111, "RRef", "Reference", 0, 0))

        );
        return Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(list).build();
    }

    private static final class NamedColors {
        private static final Map<String, Color> namedColors =
                createNamedColors();

        private NamedColors() {
        }

        private static Color get(String name) {
            return (Color) namedColors.values().toArray()[Math.abs(name.hashCode() % 147)];
        }

        private static Map<String, Color> createNamedColors() {
            Map<String, Color> colors = new HashMap<String, Color>(256);

            colors.put("aliceblue", ALICEBLUE);
            colors.put("antiquewhite", ANTIQUEWHITE);
            colors.put("aqua", AQUA);
            colors.put("aquamarine", AQUAMARINE);
            colors.put("azure", AZURE);
            colors.put("beige", BEIGE);
            colors.put("bisque", BISQUE);
            colors.put("black", BLACK);
            colors.put("blanchedalmond", BLANCHEDALMOND);
            colors.put("blue", BLUE);
            colors.put("blueviolet", BLUEVIOLET);
            colors.put("brown", BROWN);
            colors.put("burlywood", BURLYWOOD);
            colors.put("cadetblue", CADETBLUE);
            colors.put("chartreuse", CHARTREUSE);
            colors.put("chocolate", CHOCOLATE);
            colors.put("coral", CORAL);
            colors.put("cornflowerblue", CORNFLOWERBLUE);
            colors.put("cornsilk", CORNSILK);
            colors.put("crimson", CRIMSON);
            colors.put("cyan", CYAN);
            colors.put("darkblue", DARKBLUE);
            colors.put("darkcyan", DARKCYAN);
            colors.put("darkgoldenrod", DARKGOLDENROD);
            colors.put("darkgray", DARKGRAY);
            colors.put("darkgreen", DARKGREEN);
            colors.put("darkgrey", DARKGREY);
            colors.put("darkkhaki", DARKKHAKI);
            colors.put("darkmagenta", DARKMAGENTA);
            colors.put("darkolivegreen", DARKOLIVEGREEN);
            colors.put("darkorange", DARKORANGE);
            colors.put("darkorchid", DARKORCHID);
            colors.put("darkred", DARKRED);
            colors.put("darksalmon", DARKSALMON);
            colors.put("darkseagreen", DARKSEAGREEN);
            colors.put("darkslateblue", DARKSLATEBLUE);
            colors.put("darkslategray", DARKSLATEGRAY);
            colors.put("darkslategrey", DARKSLATEGREY);
            colors.put("darkturquoise", DARKTURQUOISE);
            colors.put("darkviolet", DARKVIOLET);
            colors.put("deeppink", DEEPPINK);
            colors.put("deepskyblue", DEEPSKYBLUE);
            colors.put("dimgray", DIMGRAY);
            colors.put("dimgrey", DIMGREY);
            colors.put("dodgerblue", DODGERBLUE);
            colors.put("firebrick", FIREBRICK);
            colors.put("floralwhite", FLORALWHITE);
            colors.put("forestgreen", FORESTGREEN);
            colors.put("fuchsia", FUCHSIA);
            colors.put("gainsboro", GAINSBORO);
            colors.put("ghostwhite", GHOSTWHITE);
            colors.put("gold", GOLD);
            colors.put("goldenrod", GOLDENROD);
            colors.put("gray", GRAY);
            colors.put("green", GREEN);
            colors.put("greenyellow", GREENYELLOW);
            colors.put("grey", GREY);
            colors.put("honeydew", HONEYDEW);
            colors.put("hotpink", HOTPINK);
            colors.put("indianred", INDIANRED);
            colors.put("indigo", INDIGO);
            colors.put("ivory", IVORY);
            colors.put("khaki", KHAKI);
            colors.put("lavender", LAVENDER);
            colors.put("lavenderblush", LAVENDERBLUSH);
            colors.put("lawngreen", LAWNGREEN);
            colors.put("lemonchiffon", LEMONCHIFFON);
            colors.put("lightblue", LIGHTBLUE);
            colors.put("lightcoral", LIGHTCORAL);
            colors.put("lightcyan", LIGHTCYAN);
            colors.put("lightgoldenrodyellow", LIGHTGOLDENRODYELLOW);
            colors.put("lightgray", LIGHTGRAY);
            colors.put("lightgreen", LIGHTGREEN);
            colors.put("lightgrey", LIGHTGREY);
            colors.put("lightpink", LIGHTPINK);
            colors.put("lightsalmon", LIGHTSALMON);
            colors.put("lightseagreen", LIGHTSEAGREEN);
            colors.put("lightskyblue", LIGHTSKYBLUE);
            colors.put("lightslategray", LIGHTSLATEGRAY);
            colors.put("lightslategrey", LIGHTSLATEGREY);
            colors.put("lightsteelblue", LIGHTSTEELBLUE);
            colors.put("lightyellow", LIGHTYELLOW);
            colors.put("lime", LIME);
            colors.put("limegreen", LIMEGREEN);
            colors.put("linen", LINEN);
            colors.put("magenta", MAGENTA);
            colors.put("maroon", MAROON);
            colors.put("mediumaquamarine", MEDIUMAQUAMARINE);
            colors.put("mediumblue", MEDIUMBLUE);
            colors.put("mediumorchid", MEDIUMORCHID);
            colors.put("mediumpurple", MEDIUMPURPLE);
            colors.put("mediumseagreen", MEDIUMSEAGREEN);
            colors.put("mediumslateblue", MEDIUMSLATEBLUE);
            colors.put("mediumspringgreen", MEDIUMSPRINGGREEN);
            colors.put("mediumturquoise", MEDIUMTURQUOISE);
            colors.put("mediumvioletred", MEDIUMVIOLETRED);
            colors.put("midnightblue", MIDNIGHTBLUE);
            colors.put("mintcream", MINTCREAM);
            colors.put("mistyrose", MISTYROSE);
            colors.put("moccasin", MOCCASIN);
            colors.put("navajowhite", NAVAJOWHITE);
            colors.put("navy", NAVY);
            colors.put("oldlace", OLDLACE);
            colors.put("olive", OLIVE);
            colors.put("olivedrab", OLIVEDRAB);
            colors.put("orange", ORANGE);
            colors.put("orangered", ORANGERED);
            colors.put("orchid", ORCHID);
            colors.put("palegoldenrod", PALEGOLDENROD);
            colors.put("palegreen", PALEGREEN);
            colors.put("paleturquoise", PALETURQUOISE);
            colors.put("palevioletred", PALEVIOLETRED);
            colors.put("papayawhip", PAPAYAWHIP);
            colors.put("peachpuff", PEACHPUFF);
            colors.put("peru", PERU);
            colors.put("pink", PINK);
            colors.put("plum", PLUM);
            colors.put("powderblue", POWDERBLUE);
            colors.put("purple", PURPLE);
            colors.put("red", RED);
            colors.put("rosybrown", ROSYBROWN);
            colors.put("royalblue", ROYALBLUE);
            colors.put("saddlebrown", SADDLEBROWN);
            colors.put("salmon", SALMON);
            colors.put("sandybrown", SANDYBROWN);
            colors.put("seagreen", SEAGREEN);
            colors.put("seashell", SEASHELL);
            colors.put("sienna", SIENNA);
            colors.put("silver", SILVER);
            colors.put("skyblue", SKYBLUE);
            colors.put("slateblue", SLATEBLUE);
            colors.put("slategray", SLATEGRAY);
            colors.put("slategrey", SLATEGREY);
            colors.put("snow", SNOW);
            colors.put("springgreen", SPRINGGREEN);
            colors.put("steelblue", STEELBLUE);
            colors.put("tan", TAN);
            colors.put("teal", TEAL);
            colors.put("thistle", THISTLE);
            colors.put("tomato", TOMATO);
            colors.put("transparent", TRANSPARENT);
            colors.put("turquoise", TURQUOISE);
            colors.put("violet", VIOLET);
            colors.put("wheat", WHEAT);
            colors.put("white", WHITE);
            colors.put("whitesmoke", WHITESMOKE);
            colors.put("yellow", YELLOW);
            colors.put("yellowgreen", YELLOWGREEN);

            return colors;
        }
    }

    private Ontology.Accessor $ont;
    private FuseClient fuseClient;
    private FuseResourceInfo fuseResourceInfo;

    private MultiGraph graph;
    private Viewer viewer;
    private FxDefaultView view;


}
