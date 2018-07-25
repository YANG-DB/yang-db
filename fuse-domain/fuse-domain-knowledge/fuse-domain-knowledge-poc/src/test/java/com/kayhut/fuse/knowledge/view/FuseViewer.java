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

package com.kayhut.fuse.knowledge.view;


import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext;
import com.kayhut.fuse.graph.algorithm.BetweennessCentrality;
import com.kayhut.fuse.graph.view.AssignmentToGraph;
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
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.utils.FuseClient;
import javafx.scene.paint.Color;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.util.DefaultApplication;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.start;
import static com.kayhut.fuse.graph.view.AssignmentToGraph.styleSheet;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static java.lang.Thread.sleep;


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

        BetweennessCentrality bcb = new BetweennessCentrality();
        bcb.setUnweighted();
        bcb.init(graph);
        bcb.compute();
        final double max = bcb.max(graph);

/*
        PageRank pageRank = new PageRank();
        pageRank.setVerbose(true);
        pageRank.init(graph);
*/


        while (loop) {
            pipeIn.pump();
            for (Node node : graph) {
                double factor = node.getAttribute("Cb", Double.class).doubleValue()/max;
                final Color color = node.getAttribute("ui.color", Color.class);
                node.setAttribute("ui.color",new Color(color.getRed(),color.getGreen(),color.getBlue(),factor));

//                double rank = pageRank.getRank(node);
//                if(node.getAttribute("ui.size", Float.class)!=null) {
//                    final double factor = Math.sqrt(graph.getNodeCount() * rank * 5);
//                    node.setAttribute("ui.size", node.getAttribute("ui.size", Float.class).floatValue() * factor);
//                }
            }
            sleep(5000);
        }

        System.out.println("bye bye");
        System.exit(0);


    }

    private void populateGraph(Graph g, String... values) throws IOException, InterruptedException {
        AssignmentToGraph.populateGraph(g, fetchAssignment(values));
    }

    private Assignment fetchAssignment(String... values) throws IOException, InterruptedException {
        return ((AssignmentsQueryResult) KnowledgeReaderContext.query(fuseClient, fuseResourceInfo, query($ont,values))).getAssignments().get(0);
    }

    private Query query(String... values) {
        return start().withEntity("Entity")
                .withGlobalEntity("Global")
                .withRef("Ref")
                .withValue("Val")
                .withInsight("Insight")
                .relatedTo("Rel", "SideB")
                .build();
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

    private Ontology.Accessor $ont;
    private FuseClient fuseClient;
    private FuseResourceInfo fuseResourceInfo;

    private MultiGraph graph;
    private Viewer viewer;
    private FxDefaultView view;


}
