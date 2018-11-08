package org.unipop.process.vertex;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.Profiling;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.process.bulk.DistinctBulkIterator;
import org.unipop.process.UniPredicatesStep;
import org.unipop.process.order.Orderable;
import org.unipop.query.StepDescriptor;
import org.unipop.process.predicate.ReceivesPredicatesHolder;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.*;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.EmptyIterator;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;
import org.unipop.query.search.DeferredVertexQuery;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.schema.reference.DeferredVertex;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;

import java.util.*;

public class UniGraphVertexStep<E extends Element> extends UniPredicatesStep<Vertex, E> implements ReceivesPredicatesHolder<Vertex, E>, Orderable, Profiling{
    private static final Logger logger = LoggerFactory.getLogger(UniGraphVertexStep.class);

    private final boolean returnsVertex;
    private final Direction direction;
    private Class<E> returnClass;
    private String[] edgeLabels = new String[0];
    private int limit;
    private PredicatesHolder predicates = PredicatesHolderFactory.empty();
    private StepDescriptor stepDescriptor;
    private List<SearchVertexQuery.SearchVertexController> controllers;
    private List<DeferredVertexQuery.DeferredVertexController> deferredVertexControllers;
    private List<Pair<String, Order>> orders;

    public UniGraphVertexStep(VertexStep<E> vertexStep, UniGraph graph, ControllerManager controllerManager) {
        super(vertexStep.getTraversal(), graph);
        vertexStep.getLabels().forEach(this::addLabel);
        this.direction = vertexStep.getDirection();
        this.returnClass = vertexStep.getReturnClass();
        this.returnsVertex = vertexStep.returnsVertex();
        if (vertexStep.getEdgeLabels().length > 0) {
            this.edgeLabels = vertexStep.getEdgeLabels();
            HasContainer labelsPredicate = new HasContainer(T.label.getAccessor(), P.within(vertexStep.getEdgeLabels()));
            this.predicates = PredicatesHolderFactory.predicate(labelsPredicate);
        } else this.predicates = PredicatesHolderFactory.empty();
        this.controllers = controllerManager.getControllers(SearchVertexQuery.SearchVertexController.class);
        this.deferredVertexControllers = controllerManager.getControllers(DeferredVertexQuery.DeferredVertexController.class);
        this.stepDescriptor = new StepDescriptor(this);
        limit = -1;

        //this.controllers.forEach(controller -> controller.setProfiler(this.profiler));
    }

    @Override
    protected Iterator<Traverser.Admin<E>> process() {
        return javaslang.collection.Stream.ofAll(() -> new DistinctBulkIterator<>(starts, (e) -> e.get().id(), this.bulkSizeSupplierFactory))
                .flatMap(bulk -> () -> process(bulk)).iterator();
    }

    @Override
    protected Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<Vertex>> traversers) {
        Map<Object, List<Traverser<Vertex>>> idToTraverser = new HashMap<>(traversers.size());
        Map<Object, Vertex> vertices = new HashMap<>(this.bulkSizeSupplierFactory.get().get());
        traversers.forEach(traverser -> {
            Vertex vertex = traverser.get();
            List<Traverser<Vertex>> traverserList = idToTraverser.computeIfAbsent(vertex.id(), k -> new ArrayList<>(1));
            traverserList.add(traverser);
            vertices.put(vertex.id(), vertex);
        });

        List<Vertex> uniqueVertices = Stream.ofAll(vertices.values()).toJavaList();

        SearchVertexQuery vertexQuery;
        if (!returnsVertex)
            vertexQuery = new SearchVertexQuery(Edge.class, uniqueVertices, direction, predicates, limit, propertyKeys, orders, stepDescriptor);
        else
            vertexQuery = new SearchVertexQuery(Edge.class, uniqueVertices, direction, predicates, -1, propertyKeys, null, stepDescriptor);
        //logger.debug("Executing query: ", vertexQuery);

        Iterator<Traverser.Admin<E>> traversersIterator = Stream.ofAll(this.controllers)
                .map(controller -> controller.search(vertexQuery))
                .flatMap(iterator -> () -> iterator)
                .flatMap(edge -> toTraversers(edge, idToTraverser))
                .iterator();

        if (!this.returnsVertex || (propertyKeys != null && propertyKeys.size() == 0))
            return traversersIterator;
        return getTraversersWithProperties(traversersIterator);
    }

    private Iterator<Traverser.Admin<E>> getTraversersWithProperties(Iterator<Traverser.Admin<E>> traversers) {
        List<Traverser.Admin<E>> copyTraversers = Stream.ofAll(() -> traversers).toJavaList(); // ConversionUtils.asStream(traversers).collect(Collectors.toList());

        List<DeferredVertex> deferredVertices = Stream.ofAll(copyTraversers)
                .map(Attachable::get)
                .filter(vertex -> vertex instanceof DeferredVertex)
                .map(vertex -> ((DeferredVertex)vertex))
                .filter(DeferredVertex::isDeferred)
                .toJavaList();

        if (deferredVertices.size() > 0) {
            DeferredVertexQuery query = new DeferredVertexQuery(deferredVertices, propertyKeys, orders, this.stepDescriptor);
            deferredVertexControllers.stream().forEach(controller -> controller.fetchProperties(query));
        }
        return copyTraversers.iterator();
    }

    private Iterable<Traverser.Admin<E>> toTraversers(Edge edge, Map<Object, List<Traverser<Vertex>>> traversers) {
        List<Traverser.Admin<E>> newTraversers = new ArrayList<>();

        edge.vertices(direction).forEachRemaining(originalVertex -> {
            List<Traverser<Vertex>> vertexTraversers = traversers.get(originalVertex.id());
            if (vertexTraversers != null) {
                for (Traverser<Vertex> vertexTraverser : vertexTraversers) {
                    E result = getReturnElement(edge, originalVertex);
                    newTraversers.add(vertexTraverser.asAdmin().split(result, this));
                }
            }
        });

        return newTraversers;

        /*return Stream.ofAll(() -> edge.vertices(direction))
                .flatMap(originalVertex -> {
                    List<Traverser<Vertex>> vertexTraversers = traversers.get(originalVertex.id());
                    if (vertexTraversers == null) {
                        return null;
                    }

                    return Stream.ofAll(vertexTraversers).map(vertexTraverser -> {
                        E result = getReturnElement(edge, originalVertex);
                        return vertexTraverser.asAdmin().split(result, this);
                    });
                }).filter(Objects::nonNull);*/
    }

    private E getReturnElement(Edge edge, Vertex originalVertex) {
        if (!this.returnsVertex) return (E) edge;
        return (E) UniVertex.vertexToVertex(originalVertex, edge, this.direction);
    }

    @Override
    public void reset() {
        super.reset();
        this.results = EmptyIterator.instance();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.direction, Arrays.asList(this.edgeLabels), this.returnClass.getSimpleName().toLowerCase());
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.OBJECT);
    }

    @Override
    public void addPredicate(PredicatesHolder predicatesHolder) {
        this.predicates = PredicatesHolderFactory.and(this.predicates, predicatesHolder);
    }

    @Override
    public PredicatesHolder getPredicates() {
        return predicates;
    }

    @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public void setMetrics(MutableMetrics metrics) {
        this.stepDescriptor = new StepDescriptor(this, metrics);
    }

    @Override
    public void setOrders(List<Pair<String, Order>> orders) {
        this.orders = orders;
    }

    public Class<E> getReturnClass() {
        return returnClass;
    }

    public Direction getDirection() {
        return direction;
    }
}
