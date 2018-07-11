package com.kayhut.fuse.assembly.knowledge.logical.cursor;

import com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories.LogicalElementFactory;
import com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders.LogicalModelAdderProvider;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalItemBase;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.utils.ConversionUtil;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.*;

public class KnowledgeLogicalModelTraversalCursor implements Cursor {
    //region Constructors
    public KnowledgeLogicalModelTraversalCursor(TraversalCursorContext context, Iterable<String> countTags) {
        this.countTags = Stream.ofAll(countTags).toJavaSet();
        this.distinctIds = new HashSet<>();
        this.idsScore = new HashMap<>();

        this.context = context;
        this.ont = new Ontology.Accessor(context.getOntology());
        this.typeProperty = this.ont.property$("type");

        this.includeEntities = context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.all) ||
                context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.entities);
        this.includeRelationships = context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.all) ||
                context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.relationships);

        Plan flatPlan = PlanUtil.flat(context.getQueryResource().getExecutionPlan().getPlan());
        if (this.includeEntities) {
            this.eEntityBases = Stream.ofAll(flatPlan.getOps())
                    .filter(planOp -> planOp instanceof EntityOp)
                    .map(planOp -> (EntityOp) planOp)
                    .toJavaMap(planOp -> new Tuple2<>(planOp.getAsgEbase().geteBase().geteTag(), planOp.getAsgEbase().geteBase()));
        }

        if (this.includeRelationships) {
            this.eRels = Stream.ofAll(flatPlan.getOps())
                    .filter(planOp -> planOp instanceof RelationOp)
                    .toJavaMap(planOp -> {
                        RelationOp relationOp = (RelationOp) planOp;
                        Optional<EntityOp> prevEntityOp =
                                PlanUtil.prev(flatPlan, planOp, EntityOp.class);
                        Optional<EntityOp> nextEntityOp =
                                PlanUtil.next(flatPlan, planOp, EntityOp.class);

                        String relationLabel = prevEntityOp.get().getAsgEbase().geteBase().geteTag() +
                                ConversionUtil.convertDirectionGraphic(relationOp.getAsgEbase().geteBase().getDir()) +
                                nextEntityOp.get().getAsgEbase().geteBase().geteTag();

                        return new Tuple2<>(relationLabel,
                                new Tuple3<>(prevEntityOp.get().getAsgEbase().geteBase(),
                                        relationOp.getAsgEbase().geteBase(),
                                        nextEntityOp.get().getAsgEbase().geteBase()));
                    });
        }
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {
        Map<String, LogicalItemBase> logicalItems = new HashMap<>();
        LogicalElementFactory logicalElementFactory = new LogicalElementFactory();
        LogicalModelAdderProvider logicalModelAdderProvider = new LogicalModelAdderProvider();
        try {
            while (this.distinctIds.size() < numResults) {
                Path path = context.getTraversal().next();
                List<Object> pathObjects = path.objects();

                // First vertex not always connected to the closed edge.
                Vertex vertex = (Vertex) pathObjects.get(0);
                logicalItems.put(vertex.id().toString(), logicalElementFactory.createLogicalItem(vertex));
                int objectIndex = 1;
                while (objectIndex < path.objects().size()) {
                    // Edge + next vertex
                    vertex = (Vertex) pathObjects.get(objectIndex + 1);
                    logicalItems.put(vertex.id().toString(), logicalElementFactory.createLogicalItem(vertex));
                    Edge edge = (Edge) pathObjects.get(objectIndex);
                    Vertex child = edge.inVertex();
                    Vertex parent = edge.outVertex();
                    logicalModelAdderProvider.addChild(logicalItems.get(parent.id().toString()), logicalItems.get(child.id().toString()), parent.label(), child.label());
//
//                    if (objectIndex % 2 == 0) {
//                        // Vertex
////                        Vertex vertex = (Vertex) pathObjects.get(objectIndex);
////                        LogicalItemBase logicalElement = logicalItems.get((String) vertex.id());
////                        if (logicalElement == null) {
////                            logicalItems.put((String) vertex.id(), logicalElementFactory.createLogicalItem(vertex));
////                        } else {
////                            logicalElementFactory.putLogicalItem(vertex, logicalElement);
////                        }
//                    } else {
//                        // Edge
//                        // If out or in not exist - create
//                        // then use Adder provider!
//                        Edge edge = (Edge) pathObjects.get(objectIndex);
//                        Vertex inVertex = edge.inVertex();
//                        Vertex outVertex = edge.outVertex();
//                        if (logicalItems.get((String) inVertex.id()) == null) {
//                            logicalItems.put((String) inVertex.id(), logicalElementFactory.createLogicalItem(inVertex));
//                        }
//                        if (logicalItems.get((String) outVertex.id()) == null) {
//                            logicalItems.put((String) outVertex.id(), logicalElementFactory.createLogicalItem(outVertex));
//                        }
//                        // logicalModelAdderProvider.addChild();
//                    }
                    objectIndex += 2;
                }
            }
        } catch (NoSuchElementException ex) {

        }


        Map<String, Map<Vertex, Set<String>>> idVertexEtagsMap = new HashMap<>();
        Map<String, Tuple2<Edge, String>> idEdgeEtagMap = new HashMap<>();

        Ontology.Accessor ont = new Ontology.Accessor(this.context.getOntology());
        //  LogicalModelAdderProvider logicalModelAdderProvider = new LogicalModelAdderProvider();


        try {
            while (this.distinctIds.size() < numResults) {
                Path path = context.getTraversal().next();
                List<Object> pathObjects = path.objects();
                List<Set<String>> pathLabels = path.labels();
                Set<String> addedIds = new HashSet<>();
                List<Float> eValueScores = new ArrayList<>();
                for (int objectIndex = 0; objectIndex < path.objects().size(); objectIndex++) {
                    Element element = (Element) pathObjects.get(objectIndex);
                    String pathLabel = pathLabels.get(objectIndex).iterator().next();

                    if (this.countTags.contains(pathLabel)) {
                        this.distinctIds.add(element.id().toString());
                        addedIds.add(element.id().toString());
                    }
                    if (element.label().equals("Evalue")) {
                        org.apache.tinkerpop.gremlin.structure.Property<Object> score = element.property("score");
                        if (score.isPresent()) {
                            eValueScores.add((Float) score.value());
                        }
                    }

                    if (Vertex.class.isAssignableFrom(element.getClass()) && this.includeEntities) {
                        Map<Vertex, Set<String>> vertexEtagsMap = idVertexEtagsMap.computeIfAbsent(element.id().toString(), id -> new HashMap<>());
                        vertexEtagsMap.computeIfAbsent((Vertex) element, vertex -> new HashSet<>()).add(pathLabel);
                    } else if (Edge.class.isAssignableFrom(element.getClass()) && this.includeRelationships) {
                        idEdgeEtagMap.computeIfAbsent(element.id().toString(), id -> new Tuple2<>((Edge) element, pathLabel));
                    }
                }

                if (eValueScores.size() > 0) {
                    Float score = Stream.ofAll(eValueScores).max().get();
                    addedIds.forEach(id -> {
                        this.idsScore.compute(id, (k, v) -> v == null ? score : Math.max(score, v));
                    });
                }


            }
        } catch (NoSuchElementException ex) {

        }

        Assignment.Builder builder = Assignment.Builder.instance();


        for (Map.Entry<String, Map<Vertex, Set<String>>> idVertexEtagsEntry : idVertexEtagsMap.entrySet()) {
            Vertex mergedVertex = mergeVertices(Stream.ofAll(idVertexEtagsEntry.getValue().keySet()).toJavaList());
            if (this.idsScore.containsKey(mergedVertex.id())) {
                mergedVertex.property("score", this.idsScore.get(mergedVertex.id()));
            }
            Set<String> etags = Stream.ofAll(idVertexEtagsEntry.getValue().values()).flatMap(etags1 -> etags1).toJavaSet();
            builder.withEntity(toEntity(mergedVertex, etags));
        }


        for (Map.Entry<String, Tuple2<Edge, String>> idEdgeEtagEntry : idEdgeEtagMap.entrySet()) {
            Tuple3<EEntityBase, Rel, EEntityBase> relTuple = this.eRels.get(idEdgeEtagEntry.getValue()._2());
            builder.withRelationship(toRelationship(
                    idEdgeEtagEntry.getValue()._1(),
                    relTuple._1(),
                    relTuple._2(),
                    relTuple._3()));
        }


        Assignment assignment = builder.build();
        return AssignmentsQueryResult.Builder.instance().withAssignment(assignment).build();
    }
    //endregion

    //region Private Methods
    private Entity toEntity(Vertex vertex, Set<String> eTags) {
        String eType = vertex.label();
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        Entity.Builder builder = Entity.Builder.instance();
        builder.withEID(vertex.id().toString());
        builder.withEType(eType);
        builder.withETag(eTags);
        builder.withProperties(properties);
        return builder.build();
    }

    private Relationship toRelationship(Edge edge, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.instance();
        builder.withRID(edge.id().toString());
        builder.withRType(rel.getrType());
        builder.withEID1(edge.outVertex().id().toString());
        builder.withEID2(edge.inVertex().id().toString());

        switch (rel.getDir()) {
            case R:
                builder.withETag1(prevEntity.geteTag());
                builder.withETag2(nextEntity.geteTag());
                break;

            case L:
                builder.withETag1(nextEntity.geteTag());
                builder.withETag2(prevEntity.geteTag());
        }

        return builder.build();
    }

    private Optional<Property> toProperty(VertexProperty vertexProperty) {
        return Stream.of(vertexProperty.key())
                .map(key -> this.ont.property(key))
                .filter(Optional::isPresent)
                .filter(property -> !property.get().getpType().equals(this.typeProperty.getpType()))
                .map(property -> new Property(property.get().getpType(), "raw", vertexProperty.value()))
                .toJavaOptional();
    }

    private Vertex mergeVertices(List<Vertex> vertices) {
        Vertex firstVertex = vertices.get(0);

        if (Stream.ofAll(vertices).map(vertex -> vertex.keys().size()).distinct().size() == 1) {
            return firstVertex;
        }

        Set<String> firstVertexPropertyKeys = firstVertex.keys();

        Stream.ofAll(vertices)
                .drop(1)
                .flatMap(vertex -> vertex::properties)
                .filter(property -> !firstVertexPropertyKeys.contains(property.key()))
                .distinctBy(org.apache.tinkerpop.gremlin.structure.Property::key)
                .forEach(property -> firstVertex.property(property.key(), property.value()));

        return firstVertex;
    }

    private Edge mergeEdges(List<Edge> edges) {
        return edges.get(0);
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private Set<String> countTags;
    private Set<String> distinctIds;
    private Map<String, Float> idsScore;
    private Ontology.Accessor ont;

    private com.kayhut.fuse.model.ontology.Property typeProperty;

    private Map<String, EEntityBase> eEntityBases;
    private Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> eRels;

    boolean includeEntities;
    boolean includeRelationships;
    //endregion
}
