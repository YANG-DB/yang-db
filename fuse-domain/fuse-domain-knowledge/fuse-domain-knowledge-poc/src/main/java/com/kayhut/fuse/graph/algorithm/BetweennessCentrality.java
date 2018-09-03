/*
 * Copyright 2006 - 2015
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 *
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
package com.kayhut.fuse.graph.algorithm;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;

/**
 * Compute the "betweenness" centrality of each vertex of a given graph.
 *
 * <p>
 * The betweenness centrality counts how many shortest paths between each
 * pair of nodes of the graph pass by a node. It does it for all nodes of
 * the graph.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <p>
 * This algorithm, by default, stores the centrality values for each node inside
 * the "Cb" attribute. You can change this attribute name at construction time.
 * </p>
 *
 * <p>
 * <b>This algorithm does not accept multi-graphs (p-graphs with p>1) yet.</b>
 * </p>
 *
 * <p>
 * This algorithm does not take into account edge direction yet.
 * </p>
 *
 * <p>
 * By default the
 * weight attribute name is "weight", you can activate the weights using
 * {@link #setWeighted()}. You can change the weight attribute name using the
 * dedicated constructor or the {@link #setWeightAttributeName(String)} method.
 * This method implicitly enable weights in the computation. Use
 * {@link #setUnweighted()} to disable weights.
 * </p>
 *
 * <p>
 * The result of the computation is stored on each node inside the "Cb"
 * attribute. You can change the name of this attribute using the dedicated
 * constructor or the {@link #setCentralityAttributeName(String)} method.
 * </p>
 *
 * <p>
 * As the computing of centrality can take a lot of time, you can provide a
 * progress 'callback' to get notified each time the algorithm finished
 * processing a node (however the centrality values are usable only when the
 * algorithm finished). See the {@link #registerProgressIndicator(Progress)}
 * method.
 * </p>
 *
 * <h2>Complexity</h2>
 *
 * <p>
 * By default the algorithm performs on a graph considered as not weighted with
 * complexity O(nm). You can specify that the graph edges contain weights in
 * which case the algorithm complexity is O(nm + n^2 log n).
 * </p>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * 		Graph graph = new SingleGraph("Betweenness Test");
 *
 * 		//    E----D  AB=1, BC=5, CD=3, DE=2, BE=6, EA=4
 * 		//   /|    |  Cb(A)=4
 * 		//  / |    |  Cb(B)=2
 * 		// A  |    |  Cb(C)=0
 * 		//  \ |    |  Cb(D)=2
 * 		//   \|    |  Cb(E)=4
 * 		//    B----C
 *
 * 		Node A = graph.addNode("A");
 * 		Node B = graph.addNode("B");
 * 		Node E = graph.addNode("E");
 * 		Node C = graph.addNode("C");
 * 		Node D = graph.addNode("D");
 *
 * 		graph.addEdge("AB", "A", "B");
 * 		graph.addEdge("BE", "B", "E");
 * 		graph.addEdge("BC", "B", "C");
 * 		graph.addEdge("ED", "E", "D");
 * 		graph.addEdge("CD", "C", "D");
 * 		graph.addEdge("AE", "A", "E");
 *
 * 		bcb.setWeight(A, B, 1);
 * 		bcb.setWeight(B, E, 6);
 * 		bcb.setWeight(B, C, 5);
 * 		bcb.setWeight(E, D, 2);
 * 		bcb.setWeight(C, D, 3);
 * 		bcb.setWeight(A, E, 4);
 *
 * 		BetweennessCentrality bcb = new BetweennessCentrality();
 * 		bcb.setWeightAttributeName("weight");
 * 		bcb.init(graph);
 * 		bcb.compute();
 *
 * 		System.out.println("A="+ graph.getNode("A").getAttribute("Cb"));
 * 		System.out.println("B="+ graph.getNode("B").getAttribute("Cb"));
 * 		System.out.println("C="+ graph.getNode("C").getAttribute("Cb"));
 * 		System.out.println("D="+ graph.getNode("D").getAttribute("Cb"));
 * 		System.out.println("E="+ graph.getNode("E").getAttribute("Cb"));
 * </pre>
 *
 * <h2>Reference</h2>
 *
 * <p>
 * This is based on the algorithm described in "A Faster Algorithm for
 * Betweenness Centrality", Ulrik Brandes, Journal of Mathematical Sociology,
 * 2001, and in
 * "On variants of shortest-path betweenness centrality and their generic computation",
 * of the same author, 2008.
 * </p>
 *
 * @reference A Faster Algorithm for Betweenness Centrality, Ulrik Brandes,
 * Journal of Mathematical Sociology, 2001, 25:2, pp. 163 - 177",
 * "DOI: 10.1080/0022250X.2001.9990249"
 * @reference On variants of shortest-path betweenness centrality and their generic computation,
 * Ulrik Brandes, Social Networks, vol 30:2", pp. 136 - 145, 2008,
 * issn 0378-8733, "DOI: 10.1016/j.socnet.2007.11.001".
 */
public class BetweennessCentrality implements Algorithm {

    protected static double INFINITY = 1000000.0;

    /**
     * Store the centrality value in this attribute on nodes and edges.
     */
    protected String centralityAttributeName = "Cb";

    /**
     * The predecessors.
     */
    protected String predAttributeName = "brandes.P";

    /**
     * The sigma value.
     */
    protected String sigmaAttributeName = "brandes.sigma";

    /**
     * The distance value.
     */
    protected String distAttributeName = "brandes.d";

    /**
     * The delta value.
     */
    protected String deltaAttributeName = "brandes.delta";

    /**
     * Name of the attribute used to retrieve weights on edges.
     */
    protected String weightAttributeName = "weight";

    /**
     * Do not use weights ?
     */
    protected boolean unweighted = true;

    /**
     * The graph to modify.
     */
    protected Graph graph;

    /**
     * The progress call-back method.
     */
    protected Progress progress = null;

    /**
     * Compute the centrality of edges.
     */
    protected boolean doEdges = true;

    /**
     * New centrality algorithm that will perform as if the graph was
     * unweighted. By default the centrality will be stored in a "Cb" attribute
     * on each node.
     */
    public BetweennessCentrality() {
        unweighted = true;
    }

    /**
     * New centrality algorithm that will perform as if the graph was
     * unweighted. The centrality for each node will be stored in an attribute
     * whose name is specified by the <code>centralityAttributeName</code>
     * argument.
     *
     * @param centralityAttributeName The name of the attribute used to store the result of the
     *                                algorithm on each node.
     */
    public BetweennessCentrality(String centralityAttributeName) {
        this.centralityAttributeName = centralityAttributeName;
        this.unweighted = true;
    }

    /**
     * New centrality algorithm that will perform on a weighted graph, taking
     * the weight of each edge in the given <code>weightAttributeName</code>.
     * The result of the algorithm is stored for each node using the given
     * <code>centralityAttributeName</code>. If an edge has no weight attribute,
     * it is considered as having a weight of one.
     *
     * @param centralityAttributeName Name to use to store the centrality results on each node.
     * @param weightAttributeName     Name to use to retrieve the edge weights.
     */
    public BetweennessCentrality(String centralityAttributeName,
                                 String weightAttributeName) {
        this.centralityAttributeName = centralityAttributeName;
        this.weightAttributeName = weightAttributeName;
        this.unweighted = false;
    }

    /**
     * Name of the attribute used to retrieve weights on edges.
     */
    public String getWeightAttributeName() {
        return weightAttributeName;
    }

    /**
     * Name of the attribute used to store centrality values on nodes.
     */
    public String getCentralityAttributeName() {
        return centralityAttributeName;
    }

    /**
     * Specify the name of the weight attribute to retrieve edge attributes.
     * This automatically set the algorithm to perform on the graph as if it was
     * weighted.
     */
    public void setWeightAttributeName(String weightAttributeName) {
        unweighted = false;
        this.weightAttributeName = weightAttributeName;
    }

    /**
     * Consider the edges to have a weight. By default the weight is stored in a
     * "weight" attribute. You can change this attribute using
     * {@link #setWeightAttributeName(String)}. If an edge has no weight the
     * value 1.0 is used.
     */
    public void setWeighted() {
        unweighted = false;
    }

    /**
     * Consider all the edges to have the weight.
     */
    public void setUnweighted() {
        unweighted = true;
    }

    /**
     * Activate or deactivate the centrality computation on edges. By default it is
     * activated. Notice that this does not change the complexity of the algorithm.
     * Only one more access on the edges is done to store the centrality in addition
     * to the node access.
     *
     * @param on If true, the edges centrality is also computed.
     */
    public void computeEdgeCentrality(boolean on) {
        doEdges = on;
    }

    /**
     * Specify the name of the attribute used to store the computed centrality
     * values for each node.
     */
    public void setCentralityAttributeName(String centralityAttributeName) {
        this.centralityAttributeName = centralityAttributeName;
    }

    /**
     * Specify an interface to call in order to indicate the algorithm progress.
     * Pass null to remove the progress indicator. The progress indicator will
     * be called regularly to indicate the computation progress.
     */
    public void registerProgressIndicator(Progress progress) {
        this.progress = progress;
    }

    /**
     * Setup the algorithm to work on the given graph.
     */
    public void init(Graph graph) {
        this.graph = graph;
    }

    /**
     * Compute the betweenness centrality on the given graph for each node. The
     * result is by default stored in the "Cb" attribute on each node.
     */
    public void compute() {
        if (graph != null) {
            betweennessCentrality(graph);
        }
    }

    /**
     * Compute the betweenness centrality on the given graph for each node and
     * eventually edges. This method is equivalent to a call in sequence to the
     * two methods {@link #init(Graph)} then {@link #compute()}.
     */
    public void betweennessCentrality(Graph graph) {
        init(graph);
        initAllNodes(graph);
        initAllEdges(graph);

        float n = graph.getNodeCount();
        float i = 0;

        for (Node s : graph) {
            PriorityQueue<Node> S = null;

            if (unweighted)
                S = simpleExplore(s, graph);
            else
                S = dijkstraExplore2(s, graph);

            // The really new things in the Brandes algorithm are here:
            // Accumulation phase:

            while (!S.isEmpty()) {
                Node w = S.poll();

                for (Node v : predecessorsOf(w)) {
                    double c = ((sigma(v) / sigma(w)) * (1.0 + delta(w)));
                    if (doEdges) {
                        Edge e = w.getEdgeBetween(v);
                        setCentrality(e, centrality(e) + c);
                    }
                    setDelta(v, delta(v) + c);
                }
                if (w != s) {
                    setCentrality(w, centrality(w) + delta(w));
                }
            }

            if (progress != null)
                progress.progress(i / n);

            i++;
        }
    }

    /**
     * Compute single-source multiple-targets shortest paths on an unweighted
     * graph.
     *
     * @param source The source node.
     * @param graph  The graph.
     * @return A priority queue of explored nodes with sigma values usable to
     * compute the centrality.
     */
    protected PriorityQueue<Node> simpleExplore(Node source, Graph graph) {
        LinkedList<Node> Q = new LinkedList<Node>();
        PriorityQueue<Node> S = new PriorityQueue<Node>(graph.getNodeCount(),
                new BrandesNodeComparatorLargerFirst());

        setupAllNodes(graph);
        Q.add(source);
        setSigma(source, 1.0);
        setDistance(source, 0.0);

        while (!Q.isEmpty()) {
            Node v = Q.removeFirst();

            S.add(v);
			Iterator<? extends Edge> ww = v.getLeavingEdgeIterator();
//            Iterator<? extends Edge> ww = v.getLeavingEdgeIterator();

            while (ww.hasNext()) {
                Edge l = ww.next();
                Node w = l.getOpposite(v);//ww.next();

                if (distance(w) == INFINITY) {
                    setDistance(w, distance(v) + 1);
                    Q.add(w);
                }

                if (distance(w) == (distance(v) + 1.0)) {
                    setSigma(w, sigma(w) + sigma(v));
                    addToPredecessorsOf(w, v);
                }
            }
        }

        return S;
    }

    /**
     * Compute single-source multiple-targets paths on a weighted graph.
     *
     * @param source The source node.
     * @param graph  The graph.
     * @return A priority queue of explored nodes with sigma values usable to
     * compute the centrality.
     */
    protected PriorityQueue<Node> dijkstraExplore(Node source, Graph graph) {
        PriorityQueue<Node> S = new PriorityQueue<Node>(graph.getNodeCount(),
                new BrandesNodeComparatorLargerFirst());
        PriorityQueue<Node> Q = new PriorityQueue<Node>(graph.getNodeCount(),
                new BrandesNodeComparatorSmallerFirst());

        setupAllNodes(graph);
        setDistance(source, 0.0);
        setSigma(source, 1.0);

        Q.add(source);

        while (!Q.isEmpty()) {
            Node u = Q.poll();

            if (distance(u) < 0.0) { // XXX Can happen ??? XXX
                Q.clear();
                throw new RuntimeException("negative distance ??");
            } else {
                S.add(u);

				Iterator<? extends Edge> k = u.getLeavingEdgeIterator();
//                Iterator<? extends Edge> k = u.leavingEdges().iterator();

                while (k.hasNext()) {
//					Node v = k.next();
                    Edge l = k.next();
                    Node v = l.getOpposite(u);

                    double alt = distance(u) + weight(u, v);

                    if (alt < distance(v)) {
                        if (distance(v) == INFINITY) {
                            setDistance(v, alt);
                            updatePriority(S, v);
                            updatePriority(Q, v);
                            Q.add(v);
                            setSigma(v, sigma(v) + sigma(u)); // XXX
                            // sigma(v)==0,
                            // always ?? XXX
                        } else {
                            setDistance(v, alt);
                            updatePriority(S, v);
                            updatePriority(Q, v);
                            setSigma(v, sigma(u));
                        }
                        replacePredecessorsOf(v, u);
                    } else if (alt == distance(v)) {
                        setSigma(v, sigma(v) + sigma(u));
                        addToPredecessorsOf(v, u);
                    }
                }
            }
        }

        return S;
    }

    /**
     * The implementation of the Brandes paper.
     *
     * <ul>
     * <li>title =
     * "On variants of shortest-path betweenness centrality and their generic computation"
     * ,</li>
     * <li>author = "Ulrik Brandes",</li>
     * <li>journal = "Social Networks",</li>
     * <li>volume = "30",</li>
     * <li>number = "2",</li>
     * <li>pages = "136 - 145",</li>
     * <li>year = "2008",</li>
     * <li>note = "",</li>
     * <li>issn = "0378-8733",</li>
     * <li>doi = "DOI: 10.1016/j.socnet.2007.11.001",</li> </li>
     */
    protected PriorityQueue<Node> dijkstraExplore2(Node source, Graph graph) {
        PriorityQueue<Node> S = new PriorityQueue<Node>(graph.getNodeCount(),
                new BrandesNodeComparatorLargerFirst());
        PriorityQueue<Node> Q = new PriorityQueue<Node>(graph.getNodeCount(),
                new BrandesNodeComparatorSmallerFirst());

        setupAllNodes(graph);
        setDistance(source, 0.0);
        setSigma(source, 1.0);
        Q.add(source);

        while (!Q.isEmpty()) {
            Node v = Q.poll();

            S.add(v);

			Iterator<? extends Edge> k = v.getLeavingEdgeIterator();
//            Iterator<? extends Edge> k = v.leavingEdges().iterator();

            while (k.hasNext()) {
                //Node w = k.next();
                Edge l = k.next();
                Node w = l.getOpposite(v);

                double alt = distance(v) + weight(v, w);
                double dw = distance(w);

                if (alt < dw) {
                    setDistance(w, alt);
                    updatePriority(S, w);
                    updatePriority(Q, w);
                    if (dw == INFINITY) {
                        Q.add(w);
                    }
                    setSigma(w, 0.0);
                    clearPredecessorsOf(w);
                }
                if (distance(w) == alt) {
                    setSigma(w, sigma(w) + sigma(v));
                    addToPredecessorsOf(w, v);
                }
            }
        }

        return S;
    }

    /**
     * Update the given priority queue if the given node changed its priority
     * (here distance) and if the node is already part of the queue.
     *
     * @param Q    The queue.
     * @param node The node.
     */
    protected void updatePriority(PriorityQueue<Node> Q, Node node) {
        if (Q.contains(node)) {
            Q.remove(node);
            Q.add(node);
        }
    }

    /**
     * The sigma value of the given node.
     *
     * @param node Extract the sigma value of this node.
     * @return The sigma value.
     */
    protected double sigma(Node node) {
        return node.getNumber(sigmaAttributeName);
    }

    /**
     * The distance value of the given node.
     *
     * @param node Extract the distance value of this node.
     * @return The distance value.
     */
    protected double distance(Node node) {
        return node.getNumber(distAttributeName);
    }

    /**
     * The delta value of the given node.
     *
     * @param node Extract the delta value of this node.
     * @return The delta value.
     */
    protected double delta(Node node) {
        return node.getNumber(deltaAttributeName);
    }

    /**
     * The centrality value of the given node or edge.
     *
     * @param elt Extract the centrality of this node or edge.
     * @return The centrality value.
     */
    public double centrality(Element elt) {
        return elt.getNumber(centralityAttributeName);
    }

    /**
     * List of predecessors of the given node.
     *
     * @param node Extract the predecessors of this node.
     * @return The list of predecessors.
     */
    @SuppressWarnings("all")
    protected Set<Node> predecessorsOf(Node node) {
        return (HashSet<Node>) node.getAttribute(predAttributeName);
    }

    /**
     * Set the sigma value of the given node.
     *
     * @param node  The node to modify.
     * @param sigma The sigma value to store on the node.
     */
    protected void setSigma(Node node, double sigma) {
        node.setAttribute(sigmaAttributeName, sigma);
    }

    /**
     * Set the distance value of the given node.
     *
     * @param node     The node to modify.
     * @param distance The delta value to store on the node.
     */
    protected void setDistance(Node node, double distance) {
        node.setAttribute(distAttributeName, distance);
    }

    /**
     * Set the delta value of the given node.
     *
     * @param node  The node to modify.
     * @param delta The delta value to store on the node.
     */
    protected void setDelta(Node node, double delta) {
        node.setAttribute(deltaAttributeName, delta);
    }

    /**
     * Set the centrality of the given node or edge.
     *
     * @param elt        The node or edge to modify.
     * @param centrality The centrality to store on the node.
     */
    public void setCentrality(Element elt, double centrality) {
        elt.setAttribute(centralityAttributeName, centrality);
    }

    /**
     * Set the weight of the edge between 'from' and 'to'.
     *
     * @param from   The source node.
     * @param to     The target node.
     * @param weight The weight to store on the edge between 'from' and 'to'.
     */
    public void setWeight(Node from, Node to, double weight) {
        if (from.hasEdgeBetween(to.getId()))
            from.getEdgeBetween(to.getId()).setAttribute(weightAttributeName,
                    weight);
    }

    /**
     * The weight of the edge between 'from' and 'to'.
     *
     * @param from The origin node.
     * @param to   The target node.
     * @return The weight on the edge between 'form' and 'to'.
     */
    public double weight(Node from, Node to) {
        Edge edge = from.getEdgeBetween(to.getId());

        if (edge != null) {
            if (edge.hasAttribute(weightAttributeName))
                return edge.getNumber(weightAttributeName);
            else
                return 1.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Remove all predecessors of the given node and then add it a first
     * predecessor.
     *
     * @param node        The node to modify.
     * @param predecessor The predecessor to add.
     */
    protected void replacePredecessorsOf(Node node, Node predecessor) {
        HashSet<Node> set = new HashSet<Node>();

        set.add(predecessor);
        node.setAttribute(predAttributeName, set);
    }

    /**
     * Add a node to the predecessors of another.
     *
     * @param node        Modify the predecessors of this node.
     * @param predecessor The predecessor to add.
     */
    @SuppressWarnings("all")
    protected void addToPredecessorsOf(Node node, Node predecessor) {
        HashSet<Node> preds = (HashSet<Node>) node
                .getAttribute(predAttributeName);

        preds.add(predecessor);
    }

    /**
     * Remove all predecessors of the given node.
     *
     * @param node Remove all predecessors of this node.
     */
    protected void clearPredecessorsOf(Node node) {
        HashSet<Node> set = new HashSet<Node>();
        node.setAttribute(predAttributeName, set);
    }

    /**
     * Set a default centrality of 0 to all nodes.
     *
     * @param graph The graph to modify.
     */
    protected void initAllNodes(Graph graph) {
        for (Node node : graph) {
            setCentrality(node, 0.0);
        }
    }

    /**
     * Set a default centrality of 0 to all edges.
     *
     * @param graph The graph to modify.
     */
    protected void initAllEdges(Graph graph) {
        if (doEdges) {
            graph.getEachEdge().forEach(edge -> setCentrality(edge, 0.0));
        }
    }

    /**
     * Add a default value for attributes used during computation.
     *
     * @param graph The graph to modify.
     */
    protected void setupAllNodes(Graph graph) {
        for (Node node : graph) {
            clearPredecessorsOf(node);
            setSigma(node, 0.0);
            setDistance(node, INFINITY);
            setDelta(node, 0.0);
        }
    }

    public double max(Graph graph) {
        return graph.getNodeSet().stream().max(Comparator.comparing(v -> v.getAttribute("Cb", Double.class).intValue()))
                .get().getAttribute("Cb", Double.class).doubleValue();
    }

    /**
     * Delete attributes used by this algorithm in nodes and edges of the graph
     */
    public void cleanGraph() {
        cleanElement(graph.getEachNode());
        cleanElement(graph.getEachEdge());
    }

    /**
     * Delete attributes used by this algorithm in nodes of the graph
     */
    public void cleanNodes() {
        cleanElement(graph.getEachNode());
    }

    /**
     * Delete attributes used by this algorithm in edges of the graph
     */
    public void cleanEdges() {
        cleanElement(graph.getEachEdge());
    }

    /**
     * Delete attributes used by this algorithm in elements of a graph
     *
     * @param it the list of elements
     */
    private void cleanElement(Iterable<? extends Element> it) {
        it.forEach(e -> {
            if (e.hasAttribute(predAttributeName))
                e.removeAttribute(predAttributeName);
            if (e.hasAttribute(sigmaAttributeName))
                e.removeAttribute(sigmaAttributeName);
            if (e.hasAttribute(distAttributeName))
                e.removeAttribute(distAttributeName);
            if (e.hasAttribute(deltaAttributeName))
                e.removeAttribute(deltaAttributeName);
        });
    }

    /**
     * Increasing comparator used for priority queues.
     */
    protected class BrandesNodeComparatorLargerFirst implements
            Comparator<Node> {
        public int compare(Node x, Node y) {
            // return (int) ( (distance(y)*1000.0) - (distance(x)*1000.0) );
            double yy = distance(y);
            double xx = distance(x);

            if (xx > yy)
                return -1;
            else if (xx < yy)
                return 1;

            return 0;
        }
    }

    /**
     * Decreasing comparator used for priority queues.
     */
    protected class BrandesNodeComparatorSmallerFirst implements
            Comparator<Node> {
        public int compare(Node x, Node y) {
            // return (int) ( (distance(x)*1000.0) - (distance(y)*1000.0) );
            double yy = distance(y);
            double xx = distance(x);

            if (xx > yy)
                return 1;
            else if (xx < yy)
                return -1;

            return 0;
        }
    }

    /**
     * Interface allowing to be notified of the algorithm progress.
     */
    public interface Progress {
        /**
         * Progress of the algorithm.
         *
         * @param percent a value between 0 and 1, 0 meaning 0% and 1 meaning 100%.
         */
        void progress(float percent);
    }
}
