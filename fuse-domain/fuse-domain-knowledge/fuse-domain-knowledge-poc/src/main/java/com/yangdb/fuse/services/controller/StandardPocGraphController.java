package com.yangdb.fuse.services.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.graph.algorithm.APSP;
import com.yangdb.fuse.graph.algorithm.BetweennessCentrality;
import com.yangdb.fuse.graph.algorithm.PageRank;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.ContentResponse.Builder;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.graphstream.algorithm.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.yangdb.fuse.graph.view.AssignmentToGraph.populateFromAssignment;
import static com.yangdb.fuse.services.controller.GraphBuilder.cloneGraph;
import static com.yangdb.fuse.services.controller.GraphBuilder.populate;
import static com.yangdb.fuse.services.suppliers.CachedRequestIdSupplier.RequestIdSupplierParameter;
import static org.jooby.Status.ACCEPTED;
import static org.jooby.Status.NOT_FOUND;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardPocGraphController implements PocGraphController {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final List<String> fields = Arrays.asList("type", "context", "category", "logicalId", "entityId", "entityAId", "entityBId", "refs", "fieldId", "stringValue", "dateValue", "intValue");

    private Cache<String, Graph> graph;


    //region Constructors
    @Inject
    public StandardPocGraphController(
            Cache<String, Graph> graph,
            Client client,
            RawSchema schema,
            PageDriver driver,
            @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier) {
        this.graph = graph;
        this.client = client;
        this.schema = schema;
        this.driver = driver;
        this.requestIdSupplier = requestIdSupplier;

    }


    //endregion

    private RawSchema schema;
    //region Fields
    private PageDriver driver;
    private Client client;
    private RequestIdSupplier requestIdSupplier;

    @Override
    public ContentResponse<ObjectNode> getGraphWithRank(boolean cache, String queryId, String cursorId, String pageId, @Nullable String context) {
        try {
            init(Arrays.asList(new PageRank().setVerbose(true)), cache, queryId, cursorId, pageId, context);
            final Graph graph = getGraph(queryId, cursorId, pageId, context);

            Graph queryGraph;
            final Optional<Object> data = driver.getData(queryId, cursorId, pageId);
            if (data.isPresent()) {
                AssignmentsQueryResult assignments = (AssignmentsQueryResult) data.get();
                queryGraph = populateFromAssignment(mapper, graph, assignments.getAssignments().get(0));
            } else {
                //return entire graph
                queryGraph = cloneGraph(mapper, graph, n -> n.getAttribute("type").equals("entity"), 100);
            }
            final ObjectNode node = mapper.createObjectNode();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            node.set("nodes", mapper.createArrayNode().addAll(queryGraph.getNodeSet().stream().map(this::project).collect(Collectors.toList())));
            node.set("edges", mapper.createArrayNode().addAll(queryGraph.getEdgeSet().stream().map(this::project).collect(Collectors.toList())));
            return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public ContentResponse<ObjectNode> getGraphWithRank(boolean cache, int takeTopN, @Nullable String context, String category) {
        try {
            init(Arrays.asList(new PageRank().setVerbose(true)), cache, context);
            final Graph graph = getGraph(context);

            final Graph subGraph = cloneGraph(mapper, graph, n ->
                    (n.getAttribute("type").equals("entity") &&
                            ((n.hasAttribute("context") && context != null) ? n.getAttribute("context").equals(context) : true) &&
                            ((n.hasAttribute("category") && category != null) ? n.getAttribute("category").equals(category) : true)), takeTopN);
            final ObjectNode node = mapper.createObjectNode();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            node.set("nodes", mapper.createArrayNode().addAll(subGraph.getNodeSet().stream().map(this::project).collect(Collectors.toList())));
            node.set("edges", mapper.createArrayNode().addAll(subGraph.getEdgeSet().stream().map(this::project).collect(Collectors.toList())));
            return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public ContentResponse<String> getGraphWithRankReport(boolean cache, int takeTopN, @Nullable String context, String category, String... headers) {
        init(Arrays.asList(new PageRank().setVerbose(true)), cache, context);
        final Graph graph = getGraph(context);
        StringJoiner headerRow = new StringJoiner(",").add("Rank").add("Id").add("Context").add("Category");
        Arrays.asList(headers).forEach(header -> headerRow.add(header));

        StringJoiner report = new StringJoiner(System.lineSeparator());
        report.add(headerRow.toString());

        final Graph subGraph = cloneGraph(mapper, graph, n ->
                (n.hasAttribute("type") && n.getAttribute("type").equals("entity") &&
                        ((n.hasAttribute("context") && context != null) ? n.getAttribute("context").equals(context) : true) &&
                        ((n.hasAttribute("category") && category != null) ? n.getAttribute("category").equals(category) : true)), takeTopN);
        AtomicInteger rank = new AtomicInteger();
        subGraph.getNodeSet().forEach(node -> {
            final StringJoiner line = new StringJoiner(",").add(Integer.toString(rank.getAndIncrement())).add(node.getId()).add(context).add(category);
            Arrays.asList(headers).stream().filter(header -> node.hasAttribute("value." + header))
                    .forEach(header -> line.add(node.getAttribute("value." + header)));
            report.add(line.toString());
        });
        return Builder.<String>builder(ACCEPTED, NOT_FOUND).data(Optional.of(report.toString())).compose();
    }

    @Override
    public ContentResponse<ObjectNode> getGraphPath(boolean cache, String sourceNodeId, String targetNodeId, String context) {
        init(Collections.EMPTY_LIST, cache, context);
        final Graph graph = getGraph(context);
        Dijkstra dijkstra = new Dijkstra();
        dijkstra.init(graph);
        dijkstra.setSource(graph.getNode(sourceNodeId));
        dijkstra.compute();

        List<Node> nodesList = new ArrayList<>();
        List<Edge> edgesList = new ArrayList<>();
        for (Node node : dijkstra.getPathNodes(graph.getNode(targetNodeId))) nodesList.add(node);
        for (Edge edge : dijkstra.getPathEdges(graph.getNode(targetNodeId))) edgesList.add(edge);

        final ObjectNode node = mapper.createObjectNode();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        node.set("nodes", mapper.createArrayNode().addAll(nodesList.stream().map(this::project).collect(Collectors.toList())));
        node.set("edges", mapper.createArrayNode().addAll(edgesList.stream().map(this::project).collect(Collectors.toList())));
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
    }

    @Override
    public ContentResponse<ObjectNode> getGraphWithConnectedComponents(boolean cache, int takeTopN, @Nullable String context) {
        try {
            init(Arrays.asList(new TarjanStronglyConnectedComponents()), cache, context);
            final Graph graph = getGraph(context);

            final ObjectNode node = mapper.createObjectNode();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            node.set("nodes", mapper.createArrayNode().addAll(graph.getNodeSet().stream().map(this::project).collect(Collectors.toList())));
            node.set("edges", mapper.createArrayNode().addAll(graph.getEdgeSet().stream().map(this::project).collect(Collectors.toList())));
            return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public ContentResponse<ObjectNode> getGraphWithCentroid(boolean cache, int takeTopN, @Nullable String context) {
        try {
            init(Arrays.asList(new APSP(), new Centroid()), cache, context);
            final Graph graph = getGraph(context);

            final ObjectNode node = mapper.createObjectNode();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            node.set("nodes", mapper.createArrayNode().addAll(graph.getNodeSet().stream().map(this::project).collect(Collectors.toList())));
            node.set("edges", mapper.createArrayNode().addAll(graph.getEdgeSet().stream().map(this::project).collect(Collectors.toList())));
            return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public ContentResponse<ObjectNode> getGraphWithConnectivity(boolean cache, @Nullable String context) {
        try {
            init(Arrays.asList(new BetweennessCentrality()), cache, context);
            final Graph graph = getGraph(context);

            final Graph subGraph = cloneGraph(mapper, graph, n -> n.getAttribute("type").equals("entity"), -1);
            final ObjectNode node = mapper.createObjectNode();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            node.set("nodes", mapper.createArrayNode().addAll(subGraph.getNodeSet().stream().map(this::project).collect(Collectors.toList())));
            node.set("edges", mapper.createArrayNode().addAll(subGraph.getEdgeSet().stream().map(this::project).collect(Collectors.toList())));
            return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public ContentResponse<ObjectNode> getGraphWithEccentricity(boolean cache, @Nullable String context) {
        try {
            init(Arrays.asList(new APSP(), new Eccentricity()), cache, context);
            final Graph graph = getGraph(context);

            final Graph subGraph = cloneGraph(mapper, graph, n -> n.getAttribute("type").equals("entity"), -1);
            final ObjectNode node = mapper.createObjectNode();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            node.set("nodes", mapper.createArrayNode().addAll(subGraph.getNodeSet().stream().map(this::project).collect(Collectors.toList())));
            node.set("edges", mapper.createArrayNode().addAll(subGraph.getEdgeSet().stream().map(this::project).collect(Collectors.toList())));
            return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND).data(Optional.of(node)).compose();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private void init(List<Algorithm> algorithms, boolean cache, @Nullable String ctx) {
        init(algorithms, cache, "0", "0", "0", ctx);
    }

    private void init(List<Algorithm> algorithms, boolean cache, String queryId, String cursorId, String pageId, @Nullable String ctx) {
        String context = ctx != null ? ctx : "all";
        final List<String> indices = Stream.ofAll(schema.getPartition("entity").getPartitions()).flatMap(partition -> partition.getIndices()).toJavaList();
        Graph graph = new MultiGraph(getId(queryId, cursorId, pageId, context));
        if (!cache || getGraph(queryId, cursorId, pageId, context).getNodeSet().size() == 0) {
            this.graph.put(queryId + "." + cursorId + "." + pageId, graph);
            populate(graph, client, Optional.empty(), Optional.ofNullable(ctx), fields, indices.toArray(new String[indices.size()]));
        } else {
            graph = getGraph(queryId, cursorId, pageId, context);
        }
        //run algorithms
        Graph finalGraph = graph;
        algorithms.forEach(algorithm -> {
            if (!finalGraph.hasAttribute(algorithm.getClass().getSimpleName())) {
                algorithm.init(finalGraph);
                algorithm.compute();
                finalGraph.setAttribute(algorithm.getClass().getSimpleName());
            }
        });

    }

    private String getId(String queryId, String cursorId, String pageId, @Nullable String context) {
        return queryId + "." + cursorId + "." + pageId + "." + (context != null ? context : "all");
    }

    //endregion

    private Graph getGraph(@Nullable String context) {
        return getGraph("0", "0", "0", context);
    }

    private Graph getGraph(String queryId, String cursorId, String pageId, @Nullable String context) {
        return graph.get(queryId + "." + cursorId + "." + pageId, s -> new MultiGraph(getId(queryId, cursorId, pageId, context)));
    }

    private JsonNode project(Node node) {
        final ArrayNode attributes = mapper.createArrayNode().addAll(node.getAttributeKeySet().stream()
                .map(key -> mapper.createObjectNode().put(key, node.getAttribute(key).toString())).collect(Collectors.toList()));
        return mapper.createObjectNode().put("id", node.getId()).put("degree", node.getDegree()).set("attributes", attributes);
    }

    private JsonNode project(Edge node) {
        final ArrayNode attributes = mapper.createArrayNode().addAll(node.getAttributeKeySet().stream()
                .map(key -> mapper.createObjectNode().put(key,
                        node.getAttribute(key).toString())).collect(Collectors.toList()));
        return mapper.createObjectNode().put("id", node.getId())
                .put("entityA", node.getSourceNode().getId())
                .put("entityB", node.getTargetNode().getId())
                .set("attributes", attributes);
    }
}
