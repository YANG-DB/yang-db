package com.yangdb.fuse.assembly.knowledge.cursor;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.executor.utils.ConversionUtil;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

public class KnowledgeGraphHierarchyTraversalCursor implements Cursor<TraversalCursorContext> {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new KnowledgeGraphHierarchyTraversalCursor(
                    (TraversalCursorContext)context,
                    ((CreateGraphHierarchyCursorRequest)context.getCursorRequest()).getCountTags());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public KnowledgeGraphHierarchyTraversalCursor(TraversalCursorContext context, Iterable<String> countTags) {
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
        Map<String, Map<Vertex, Set<String>>> idVertexEtagsMap = new HashMap<>();
        Map<String, Tuple2<Edge, String>> idEdgeEtagMap = new HashMap<>();

        try {
            while(this.distinctIds.size() < numResults) {
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
                    if(element.label().equals("Evalue")){
                        org.apache.tinkerpop.gremlin.structure.Property<Object> score = element.property("score");
                        if(score.isPresent()) {
                            eValueScores.add((Float) score.value());
                        }
                    }

                    if (Vertex.class.isAssignableFrom(element.getClass()) && this.includeEntities) {
                        Map<Vertex, Set<String>> vertexEtagsMap = idVertexEtagsMap.computeIfAbsent(element.id().toString(), id -> new HashMap<>());
                        vertexEtagsMap.computeIfAbsent((Vertex)element, vertex -> new HashSet<>()).add(pathLabel);
                    } else if (Edge.class.isAssignableFrom(element.getClass()) && this.includeRelationships) {
                        idEdgeEtagMap.computeIfAbsent(element.id().toString(), id -> new Tuple2<>((Edge)element, pathLabel));
                    }
                }

                if(eValueScores.size() > 0){
                    Float score = Stream.ofAll(eValueScores).max().get();
                    addedIds.forEach(id -> {
                        this.idsScore.compute(id, (k, v) -> v == null ? score : Math.max(score, v));
                    });
                }
            }
        } catch (NoSuchElementException ex) {
            //no more elements found in traversal
        }

        Assignment.Builder builder = Assignment.Builder.instance();
        for(Map.Entry<String, Map<Vertex, Set<String>>> idVertexEtagsEntry : idVertexEtagsMap.entrySet()) {
            Vertex mergedVertex = mergeVertices(Stream.ofAll(idVertexEtagsEntry.getValue().keySet()).toJavaList());
            if(this.idsScore.containsKey(mergedVertex.id())) {
                mergedVertex.property("score", this.idsScore.get(mergedVertex.id()));
            }
            Set<String> etags = Stream.ofAll(idVertexEtagsEntry.getValue().values()).flatMap(etags1 -> etags1).toJavaSet();
            builder.withEntity(toEntity(mergedVertex, etags));
        }




        for(Map.Entry<String, Tuple2<Edge, String>> idEdgeEtagEntry : idEdgeEtagMap.entrySet()) {
            Tuple3<EEntityBase, Rel, EEntityBase> relTuple = this.eRels.get(idEdgeEtagEntry.getValue()._2());
            builder.withRelationship(toRelationship(
                    idEdgeEtagEntry.getValue()._1(),
                    relTuple._1(),
                    relTuple._2(),
                    relTuple._3()));
        }


        Assignment assignment = compose(builder);
        final Query pattern = getContext().getQueryResource().getQuery();
        return AssignmentsQueryResult.Builder.instance()
                .withPattern(pattern)
                .withQueryId(context.getQueryResource().getQueryMetadata().getId())
                .withCursorId(context.getQueryResource().getCurrentCursorId())
                .withTimestamp(context.getQueryResource().getQueryMetadata().getCreationTime())
                .withAssignment(assignment).build();
    }

    protected Assignment compose(Assignment.Builder builder) {
        return builder.build();
    }
    //endregion

    //region Private Methods
    protected Entity toEntity(Vertex vertex, Set<String> eTags) {
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

    protected Relationship toRelationship(Edge edge, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.instance();
        List<Property> properties = Stream.ofAll(edge::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();
        builder.withRID(edge.id().toString());
        builder.withRTag(rel.geteTag());
        builder.withRType(rel.getrType());
        builder.withEID1(edge.outVertex().id().toString());
        builder.withEID2(edge.inVertex().id().toString());
        builder.withProperties(properties);
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

    private Optional<Property> toProperty(org.apache.tinkerpop.gremlin.structure.Property vertexProperty) {
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

    @Override
    public TraversalCursorContext getContext() {
        return context;
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

    private com.yangdb.fuse.model.ontology.Property typeProperty;

    protected Map<String, EEntityBase> eEntityBases;
    protected Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> eRels;

    boolean includeEntities;
    boolean includeRelationships;
    //endregion
}
