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
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.stream.SinkAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * All-pair shortest paths lengths.
 * 
 * <p>
 * This class implements the Floyd-Warshall all pair shortest path algorithm
 * where the shortest path from any node to any destination in a given weighted
 * graph (with positive or negative edge weights) is performed.
 * </p>
 * <p>
 * The computational complexity is O(n^3), this may seems a very large, however
 * this algorithm may perform better than running several Dijkstra on all node
 * pairs of the graph (that would be of complexity O(n^2 log(n))) when the graph
 * becomes dense.
 * </p>
 * <p>
 * Note that all the possible paths are not explicitly computed and stored.
 * Instead, the weight is computed and a data structure similar to network
 * routing tables is created directly on the graph. This allows a linear
 * reconstruction of the wanted paths, on demand, minimizing the memory
 * consumption.
 * </p>
 * <p>
 * For each node of the graph, a {@link APSP.APSPInfo} attribute is stored. The name of
 * this attribute is {@link APSP.APSPInfo#ATTRIBUTE_NAME}.
 * </p>
 * <h2>Usage</h2>
 * <p>
 * The implementation of this algorithm is made with two main classes that
 * reflect the two main steps of the algorithm that are:
 * </p>
 * <ol>
 * <li>compute pairwise weights for all nodes;</li>
 * <li>retrieve actual paths from some given sources to some given destinations.
 * </li>
 * </ol>
 * 
 * <p>
 * For the first step (the real shortest path computation) you need to create an
 * APSP object with 3 parameters:
 * </p>
 * 
 * <ul>
 * <li>a reference to the graph to be computed;</li>
 * <li>a string that indicates the name of the attribute to consider for the
 * weighting;</li>
 * <li>a boolean that indicates whether the computation considers edges
 * direction or not.</li>
 * </ul>
 * 
 * <p>
 * Those 3 parameters can be set in a ran in the constructor
 * {@link #APSP(Graph,String,boolean)} or by using separated setters (see example
 * below).
 * </p>
 * <p>
 * Then the actual computation takes place by calling the {@link #compute()} method
 * which is implemented from the "Algorithm" interface. This method actually
 * does the computation.
 * </p>
 * <p>
 * Secondly, when the weights are computed, one can retrieve paths with the help
 * of another class: "APSPInfo". Such object are stored in each node and hold
 * routing tables that can help rebuild shortest paths.
 * </p>
 * <p>
 * Retrieving an "APSPInfo" instance from a node is done for instance for a
 * node of id "F", like this::
 * </p>
 * 
 * <pre>
 * APSPInfo info = graph.getNode("F").getAttribute(APSPInfo.ATTRIBUTE_NAME);
 * </pre>
 * <p>
 * then the shortest path from a "F" to another node (say "A") is given by::
 * </p>
 * 
 * <pre>
 * info.getShortestPathTo("A")
 * </pre>
 * 
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * import java.io.ByteArrayInputStream;
 * import java.io.IOException;
 * 
 * import org.graphstream.algorithm.APSP;
 * import org.graphstream.algorithm.APSP.APSPInfo;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * import org.graphstream.stream.file.FileSourceDGS;
 * 
 * public class APSPTest {
 * 
 * //     B-(1)-C
 * //    /       \
 * //  (1)       (10)
 * //  /           \
 * // A             F
 * //  \           /
 * //  (1)       (1)
 * //    \       /
 * //     D-(1)-E
 * 
 * 	static String my_graph = "DGS004\n" 
 * 			+ "my 0 0\n" 
 * 			+ "an A \n" 
 * 			+ "an B \n"
 * 			+ "an C \n" 
 * 			+ "an D \n" 
 * 			+ "an E \n" 
 * 			+ "an F \n"
 * 			+ "ae AB A B weight:1 \n" 
 * 			+ "ae AD A D weight:1 \n"
 * 			+ "ae BC B C weight:1 \n" 
 * 			+ "ae CF C F weight:10 \n"
 * 			+ "ae DE D E weight:1 \n" 
 * 			+ "ae EF E F weight:1 \n";
 * 
 * 	public static void main(String[] args) throws IOException {
 * 		Graph graph = new DefaultGraph("APSP Test");
 * 		ByteArrayInputStream bs = new ByteArrayInputStream(my_graph.getBytes());
 * 
 * 		FileSourceDGS source = new FileSourceDGS();
 * 		source.addSink(graph);
 * 		source.readAll(bs);
 * 
 * 		APSP apsp = new APSP();
 * 		apsp.init(graph); // registering apsp as a sink for the graph
 * 		apsp.setDirected(false); // undirected graph
 * 		apsp.setWeightAttributeName("weight"); // ensure that the attribute name
 * 												// used is "weight"
 * 		apsp.compute(); // the method that actually computes shortest paths
 * 
 * 		APSPInfo info = graph.getNode("F")
 * 				.getAttribute(APSPInfo.ATTRIBUTE_NAME);
 * 		System.out.println(info.getShortestPathTo("A"));
 * 	}
 * }
 * </pre>
 * 
 * <h2>Other Features</h2>
 * 
 * <h3>Digraphs</h3>
 * <p>
 * This algorithm can use directed graphs and only compute paths according to
 * this direction. You can choose to ignore edge orientation by calling
 * {@link #setDirected(boolean)} method with "false" as value (or use the
 * appropriate constructor).
 * </p>
 * 
 * 
 * <h2>Shortest Paths with weighted edges</2>
 * <p>
 * You can also specify that edges have "weights" or "importance" that value
 * them. You store these values as attributes on the edges. The default name for
 * these attributes is "weight" but you can specify it using the
 * {@link #setWeightAttributeName(String)} method (or by using the appropriate
 * constructor). The weight attribute must contain an object that implements
 * java.lang.Number.
 * </p>
 * 
 * <h2>How shortest paths are stored in the graph?</h2>
 * <p>
 * All the shortest paths are not literally stored in the graph because it would
 * require to much memory to do so. Instead, only useful data, allowing the fast
 * reconstruction of any path, is stored. The storage approach is alike network
 * routing tables where each node maintains a list of all possible targets
 * linked with the next hop neighbor to go through.
 * </p>
 * <p>
 * Technically, on each node, for each target, we only store the target node
 * name and if the path is made of more than one edge, one "pass-by" node. As
 * all shortest path that is made of more than one edge is necessarily made of
 * two other shortest paths, it is easy to reconstruct a shortest path between
 * two arbitrary nodes knowing only a pass-by node. This approach still stores a
 * lot of data on the graph, however far less than if we stored complete paths.
 * </p>
 * 
 * @complexity O(n^3) with n the number of nodes.
 * 
 * @reference Floyd, Robert W. "Algorithm 97: Shortest Path". Communications of
 *            the ACM 5 (6): 345. doi:10.1145/367766.368168. 1962.
 * @reference Warshall, Stephen. "A theorem on Boolean matrices". Journal of the
 *            ACM 9 (1): 11–12. doi:10.1145/321105.321107. 1962.
 * 
 */
public class APSP extends SinkAdapter implements Algorithm {
	// Attribute

	/**
	 * The graph to use.
	 */
	protected Graph graph;

	/**
	 * Does the graph changed between two calls to {@link #compute()}?.
	 */
	protected boolean graphChanged = false;

	/**
	 * If false, do not take edge orientation into account.
	 */
	protected boolean directed = true;

	/**
	 * Name of the attribute on each edge indicating the weight of the edge.
	 * This attribute must contain a descendant of Number.
	 */
	protected String weightAttributeName;
	
	protected Progress progress = null;

	// Construction

	public APSP() {
		this(null);
	}

	/**
	 * New APSP algorithm working on the given graph. The edge weight attribute
	 * name by default is "weight" and edge orientation is taken into account.
	 * 
	 * @param graph
	 *            The graph to use.
	 */
	public APSP(Graph graph) {
		this(graph, "weight", true);
	}

	/**
	 * New APSP algorithm working on the given graph. To fetch edges importance,
	 * the algorithm use the given string as attribute name for edge weights.
	 * Weights must be a descendant of Number.
	 * 
	 * @param graph
	 *            The graph to use.
	 * @param weightAttributeName
	 *            The edge weight attribute name.
	 * @param directed
	 *            If false, edge orientation is ignored.
	 */
	public APSP(Graph graph, String weightAttributeName, boolean directed) {
		this.graph = graph;
		this.weightAttributeName = weightAttributeName;
		this.directed = directed;

		init(graph);
	}

	// Access

	/**
	 * True if the algorithm must take edge orientation into account.
	 * 
	 * @return True if directed.
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * The name of the attribute to use for retrieving edge weights.
	 * 
	 * @return An attribute name.
	 */
	public String getWeightAttributeName() {
		return weightAttributeName;
	}

	/**
	 * Access to the working graph.
	 * 
	 * @return graph being used
	 */
	public Graph getGraph() {
		return graph;
	}

	// Commands

	/**
	 * Choose to use or ignore edge orientation.
	 * 
	 * @param on
	 *            If true edge orientation is used.b
	 */
	public void setDirected(boolean on) {
		directed = on;
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
	 * Choose the name of the attribute used to retrieve edge weights. Edge
	 * weights attribute must contain a value that inherit Number.
	 * 
	 * @param name
	 *            The attribute name.
	 */
	public void setWeightAttributeName(String name) {
		weightAttributeName = name;
	}

	/**
	 * @see Algorithm#init(Graph)
	 */
	public void init(Graph graph) {
		if (this.graph != null)
			this.graph.removeSink(this);

		this.graph = graph;

		if (this.graph != null){
			graphChanged = true;
			this.graph.addSink(this);
		}
	}

	/**
	 * Run the APSP computation. When finished, the graph is equipped with
	 * specific attributes of type
	 * {@link APSP.APSPInfo}. These attributes contain
	 * a map of length toward each other attainable node. The attribute name is
	 * given by {@link APSP.APSPInfo#ATTRIBUTE_NAME}.
	 * 
	 * @complexity O(n^3) where n is the number of nodes in the graph.
	 */
	public void compute() {
		if (graphChanged) {
			// Make a list of all nodes, and equip them with APSP informations.
			// The APSPInfo constructor add in each info item all the paths from
			// the node to all its neighbour. It set the distance to 1 if there
			// are no weights on edges.

			ArrayList<Node> nodeList = new ArrayList<Node>();

			for (Node node : graph) {
				node.addAttribute(APSPInfo.ATTRIBUTE_NAME, new APSPInfo(node,
						weightAttributeName, directed));
				nodeList.add(node);
			}

			// The Floyd-Warshall algorithm. You can easily see it is in O(n^3)..

			int z = 0;
			double prog = 0;
			double max  = nodeList.size();
			max *= max;
			int n = nodeList.size();
			for (Node k : nodeList) {
				for (Node i : nodeList) {
					for (Node j : nodeList) {
						APSPInfo I = (APSPInfo) i.getAttribute(
								APSPInfo.ATTRIBUTE_NAME, APSPInfo.class);
						APSPInfo J = (APSPInfo) j.getAttribute(
								APSPInfo.ATTRIBUTE_NAME, APSPInfo.class);
						APSPInfo K = (APSPInfo) k.getAttribute(
								APSPInfo.ATTRIBUTE_NAME, APSPInfo.class);

						double Dij = I.getLengthTo(J.source.getId());
						double Dik = I.getLengthTo(K.source.getId());
						double Dkj = K.getLengthTo(J.source.getId());

						// Take into account non-existing paths.

						if (Dik >= 0 && Dkj >= 0) {
							double sum = Dik + Dkj;

							if (Dij >= 0) {
								if (sum < Dij) {
									I.setLengthTo(J, sum, K);
								}
							} else {
								I.setLengthTo(J, sum, K);
							}
						}
					}
					
					if (progress != null)
						progress.progress(prog / max);
					
					prog += 1;
				}

				 z++;
				 System.err.printf( "%3.2f%%%n", (z/((double)n))*100 );
			}
		}

		graphChanged = false;
	}

	/**
	 * Information stored on each node of the graph giving the length of the
	 * shortest paths toward each other node.
	 */
	public static class APSPInfo {
		public static final String ATTRIBUTE_NAME = "APSPInfo";

		/**
		 * The start node name. This information is stored inside this node.
		 */
		public Node source;

		/**
		 * Maximum number of hops to attain another node in the graph from the
		 * "from" node.
		 * XXX this is the maximum value seen during compute not the maximum shortest path XXX
		 */
		public double maxLength = Double.MIN_VALUE;

		/**
		 * Minimum number of hops to attain another node in the graph from the
		 * "from" node.
		 * XXX this is the minimum value seen during compute not the minimum shortest path XXX
		 */
		public double minLength = Double.MAX_VALUE;

		/**
		 * Shortest paths toward all other accessible nodes.
		 */
		public HashMap<String, TargetPath> targets = new HashMap<String, TargetPath>();

		/**
		 * Create the new information and put in it all the paths between this
		 * node and all its direct neighbours.
		 * 
		 * @param node
		 *            The node to start from.
		 * @param weightAttributeName
		 *            The key used to retrieve the weight attributes of edges.
		 *            This attribute but store a value that inherit Number.
		 * @param directed
		 *            If false, the edge orientation is not taken into account.
		 */
		public APSPInfo(Node node, String weightAttributeName, boolean directed) {
			double weight = 1;
			Iterable<? extends Edge> edges = node.getLeavingEdgeSet();

			source = node;

			if (!directed)
				edges = node.getEdgeSet();

			for (Edge edge : edges) {
				Node other = edge.getOpposite(node);

				if (edge.hasAttribute(weightAttributeName))
					weight = edge.getNumber(weightAttributeName);

				targets.put(other.getId(), new TargetPath(other, weight, null));
			}
		}

		/**
		 * The node represented by this APSP information.
		 * 
		 * @return A node identifier.
		 */
		public String getNodeId() {
			return source.getId();
		}

		/**
		 * Minimum distance between this node and another. This returns -1 if
		 * there is no path stored yet between these two nodes.
		 * 
		 * @param other
		 *            The other node identifier.
		 * @return The distance or -1 if no path is stored yet between the two
		 *         nodes.
		 */
		public double getLengthTo(String other) {
			if (targets.containsKey(other))
				return targets.get(other).distance;

			return -1;
		}

		/**
		 * The minimum distance between this node and another.
		 * XXX this is the minimum value seen during compute not the minimum shortest path XXX
		 * 
		 * @return A distance.
		 */
		public double getMinimumLength() {
			return minLength;
		}

		/**
		 * The maximum distance between this node and another.
		 * XXX this is the maximum value seen during compute not the maximum shortest path XXX
		 * 
		 * @return A distance.
		 */
		public double getMaximumLength() {
			return maxLength;
		}

		/**
		 * Add or change the length between this node and another and update the
		 * minimum and maximum lengths seen so far.
		 * 
		 * @param other
		 *            The other node APSP info.
		 * @param length
		 *            The new minimum path lengths between these nodes.
		 */
		public void setLengthTo(APSPInfo other, double length, APSPInfo passBy) {
			targets.put(other.source.getId(), new TargetPath(other.source,
					length, passBy));

			if (length < minLength)
				minLength = length;

			if (length > maxLength)
				maxLength = length;
		}

		public Path getShortestPathTo(String other) {
			TargetPath tpath = targets.get(other);

			// XXX Probably a bug here in the Path class usage.
			// TODO update this to create an edge path to be compatible with
			// multi-graphs.

			if (tpath != null) {
				Path path = new Path(); // XXX use the Path object directly.
				ArrayList<Node> nodePath = new ArrayList<Node>();

				nodePath.add(source);
				nodePath.add(tpath.target);

				// Recursively build the path between the source and target node
				// by exploring pass-by nodes.

				expandPath(1, this, tpath, nodePath);

				// Build a Path object.

				for (int i = 0; i < nodePath.size() - 1; ++i) {
					// XXX XXX complicated ?

					path.add(
							nodePath.get(i),
							nodePath.get(i).getEdgeToward(
									nodePath.get(i + 1).getId()));
				}

				return path;
			}

			return null;
		}

		protected int expandPath(int pos, APSPInfo source, TargetPath path,
				ArrayList<Node> nodePath) {
			// result = will contain the expanded path.
			// source = A.
			// path.passBy = X.
			// path.target = B.
			// pos = position of insertion of X inside result.

			if (path.passBy != null) {
				// We want to insert X between A and B.

				nodePath.add(pos, path.passBy.source);

				// We build paths between A and X and between X and B.

				TargetPath path1 = source.targets.get(path.passBy.source
						.getId());	// path from A -> X
				TargetPath path2 = path.passBy.targets.get(path.target.getId());
									// path from X -> B

				// Now we recurse the path expansion.

				int added1 = expandPath(pos, source, path1, nodePath);
				int added2 = expandPath(pos + 1 + added1, path.passBy, path2,
						nodePath);

				// Return the number of elements added at pos.

				return added1 + added2 + 1;
			} else {
				// These is no more intermediary node X, stop the recursion.

				return 0;
			}
		}
	}

	/**
	 * Description of a path to a target node.
	 * 
	 * <p>
	 * This class is made to be used by the APSPInfo class, which references a
	 * source node. This class describes a target node, the length of the
	 * shortest path to it and, if the path is made of more than only one edge,
	 * an intermediary node (pass-by), used to reconstruct recursively the
	 * shortest path.
	 * </p>
	 * 
	 * <p>
	 * This representation avoids to store each node of each shortest path,
	 * since this would consume a too large memory area. This way, a shortest
	 * path is stored at constant size (this is possible since we computed all
	 * the shortest paths and, knowing that a path of more than one edge is
	 * always made of the sum of two shortest paths, and knowing only one
	 * "pass-by" node in the shortest path, it is possible to rebuild it).
	 * </p>
	 */
	public static class TargetPath {
		/**
		 * A distant other node.
		 */
		public Node target;

		/**
		 * The distance to this other node.
		 */
		public double distance;

		/**
		 * An intermediary other node on the minimum path to the other node.
		 * Used to reconstruct the path between two nodes.
		 */
		public APSPInfo passBy;

		public TargetPath(Node other, double distance, APSPInfo passBy) {
			this.target = other;
			this.distance = distance;
			this.passBy = passBy;
		}
	}

	// Sink implementation

	@Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		graphChanged = true;
	}

	@Override
	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		graphChanged = true;
	}

	@Override
	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		graphChanged = true;
	}

	@Override
	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		graphChanged = true;
	}

	@Override
	public void graphCleared(String graphId, long timeId) {
		graphChanged = true;
	}

	@Override
	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value) {
		if (attribute.equals(weightAttributeName)) {
			graphChanged = true;
		}
	}

	@Override
	public void edgeAttributeChanged(String graphId, long timeId,
			String edgeId, String attribute, Object oldValue, Object value) {
		if (attribute.equals(weightAttributeName)) {
			graphChanged = true;
		}
	}

	/**
	 * Interface allowing to be notified of the algorithm progress.
	 */
	public interface Progress {
		/**
		 * Progress of the algorithm.
		 * 
		 * @param percent
		 *            a value between 0 and 1, 0 meaning 0% and 1 meaning 100%.
		 */
		void progress(double percent);
	}
}