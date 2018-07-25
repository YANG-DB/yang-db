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


import com.kayhut.fuse.graph.algorithm.BetweennessCentrality;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.fuse.utils.FuseClient;
import javafx.scene.paint.Color;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kayhut.fuse.graph.view.AssignmentToGraph.styleSheet;
import static java.lang.Thread.sleep;
import static org.elasticsearch.index.query.QueryBuilders.*;


public class FuseGraphConnectivityViewer implements ViewerListener {
    private boolean loop = true;

    public static void main(String[] args) throws Exception {
        System.setProperty("org.graphstream.ui", "org.graphstream.ui.javafx.util.Display");
        new FuseGraphConnectivityViewer().run();
//        Application.launch(FuseViewer.class);
    }

    public FuseGraphConnectivityViewer() throws IOException {
        fuseClient = new FuseClient("http://localhost:8888/fuse");
        fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        graph = new MultiGraph("g1");
    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
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

    private void populateGraph(Graph graph) {
        fetchGraph(graph);
    }


    private void fetchGraph(Graph g) {
        final TransportClient client = ElasticEmbeddedNode.getClient("knowledge", 9300);
        QueryBuilder qb = boolQuery().filter(
                boolQuery().mustNot(boolQuery()
                                .should(existsQuery("deleteUser"))
                                .should(termQuery("direction","out"))
                ));

        SearchResponse scrollResp = client.prepareSearch("e*")
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setFetchSource(new String[]{"logicalId","entityId","entityAId","entityBId","refs"}, null)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(1000).get(); //max of 100 hits will be returned for each scroll
//Scroll until no hits are returned
        do {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                final String id = hit.getId();
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        } while(scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
    }


    private Ontology.Accessor $ont;
    private FuseClient fuseClient;
    private FuseResourceInfo fuseResourceInfo;

    private MultiGraph graph;
    private Viewer viewer;
    private FxDefaultView view;


}
