package com.kayhut.fuse.assembly.knowledge.logical.cursor;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories.LogicalElementFactory;
import com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders.LogicalModelAdderProvider;
import com.kayhut.fuse.assembly.knowledge.logical.model.ElementBaseLogical;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
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
import com.kayhut.fuse.model.transport.cursor.CreateLogicalGraphHierarchyCursorRequest;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.*;

public class KnowledgeLogicalModelTraversalCursor implements Cursor {
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new KnowledgeLogicalModelTraversalCursor(
                    (TraversalCursorContext) context,
                    ((CreateLogicalGraphHierarchyCursorRequest) context.getCursorRequest()).getCountTags());
        }
        //endregion
    }

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
        Map<String, ElementBaseLogical> logicalItems = new HashMap<>();
        LogicalElementFactory logicalElementFactory = new LogicalElementFactory();
        LogicalModelAdderProvider logicalModelAdderProvider = new LogicalModelAdderProvider();
        try {
            while (this.distinctIds.size() < numResults) {
                Path path = context.getTraversal().next();
                List<Object> pathObjects = path.objects();

                // First vertex not always connected to the closed edge.
                Vertex vertex = (Vertex) pathObjects.get(0);
                addVertexToLogicalItems(logicalItems, logicalElementFactory, vertex);
                int objectIndex = 1;
                while (objectIndex < path.objects().size()) {
                    // Edge + next vertex
                    vertex = (Vertex) pathObjects.get(objectIndex + 1);
                    addVertexToLogicalItems(logicalItems, logicalElementFactory, vertex);
                    Edge edge = (Edge) pathObjects.get(objectIndex);
                    Vertex child = edge.inVertex();
                    Vertex parent = edge.outVertex();
                    logicalModelAdderProvider.addChild(
                            getLogicalElement(logicalItems, parent),
                            getLogicalElement(logicalItems, child),
                            parent.label(),
                            child.label());
                    objectIndex += 2;
                }
            }
        } catch (NoSuchElementException ex) {

        }
        // TODO : return logical result
        return null;
    }
    //endregion

    //region Private Methods

    private ElementBaseLogical getLogicalElement(Map<String, ElementBaseLogical> logicalItems, Vertex vertex){
        if(vertex.label().equals(ETypes.LOGICAL_ENTITY)){
            return logicalItems.get(vertex.id().toString() + ".global");
        }
        return logicalItems.get(vertex.id().toString());
    }

    private void addVertexToLogicalItems(Map<String, ElementBaseLogical> logicalItems, LogicalElementFactory logicalElementFactory, Vertex vertex) {
        String id = vertex.id().toString();
        // Global entity special treatment
        String globalEntitySuffix = ".global";
        if (vertex.label().equals(ETypes.LOGICAL_ENTITY)) {
            id += globalEntitySuffix;
        }

        if(!logicalItems.containsKey(id)){
            logicalItems.put(id, logicalElementFactory.createLogicalItem(vertex));
        }
        else{
            // On global entity, if already exist, should merge vertexes
            // Also, on entity value - adding value to an existing field
            if((id.contains(globalEntitySuffix) || vertex.label().equals(ETypes.ENTITY_VALUE))){
                logicalItems.put(id, logicalElementFactory.mergeLogicalItemWithVertex(vertex, logicalItems.get(id)));
            }
        }
    }


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
