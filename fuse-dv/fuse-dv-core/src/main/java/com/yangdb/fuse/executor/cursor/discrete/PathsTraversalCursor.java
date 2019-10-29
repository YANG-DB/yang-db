package com.yangdb.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
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
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

import static com.yangdb.fuse.model.results.AssignmentsQueryResult.Builder.instance;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class PathsTraversalCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new PathsTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context) {
        this.context = context;
        this.ont = new Ontology.Accessor(context.getOntology());

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

        this.typeProperty = this.ont.property$("type");
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        return toQuery(numResults);
    }
    //endregion

    //region Properties
    public TraversalCursorContext getContext() {
        return context;
    }
    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        final Query pattern = context.getQueryResource().getQuery();
        builder.withPattern(pattern);
        //build assignments
        (context.getTraversal().next(numResults)).forEach(path -> {
            builder.withAssignment(toAssignment(path));
        });
        return builder.build();
    }

    protected Assignment toAssignment(Path path) {
        Assignment.Builder builder = Assignment.Builder.instance();

        List<Object> pathObjects = path.objects();
        List<Set<String>> pathlabels = path.labels();
        for (int objectIndex = 0; objectIndex < pathObjects.size(); objectIndex++) {
            Object pathObject = pathObjects.get(objectIndex);
            String pathLabel = pathlabels.get(objectIndex).iterator().next();

            if (Vertex.class.isAssignableFrom(pathObject.getClass()) && this.includeEntities) {
                builder.withEntity(toEntity((Vertex) pathObject, this.eEntityBases.get(pathLabel)));
            } else if (Edge.class.isAssignableFrom(pathObject.getClass()) && this.includeRelationships) {
                Tuple3<EEntityBase, Rel, EEntityBase> relTuple = this.eRels.get(pathLabel);
                builder.withRelationship(toRelationship(
                        (Edge) pathObject,
                        relTuple._1(),
                        relTuple._2(),
                        relTuple._3()));
            } else {
                throw new UnsupportedOperationException("unexpected object in path");
            }
        }

        return builder.build();
    }

    protected Entity toEntity( Vertex vertex, EEntityBase element) {
        String eType = vertex.label();
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        Entity.Builder builder = Entity.Builder.instance();
        builder.withEID(vertex.id().toString());
        builder.withEType(eType);
        builder.withETag(new HashSet<>(Collections.singletonList(element.geteTag())));
        builder.withProperties(properties);
        return builder.build();
    }

    protected Relationship toRelationship(Edge edge, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.instance();
        builder.withRID(edge.id().toString());
        builder.withRType(rel.getrType());
        builder.withEID1(edge.outVertex().id().toString());
        builder.withEID2(edge.inVertex().id().toString());
        //add properties
        List<Property> properties = Stream.ofAll(edge::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

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

    protected Optional<Property> toProperty(org.apache.tinkerpop.gremlin.structure.Property vertexProperty) {
        return Stream.of(vertexProperty.key())
                .map(key -> this.ont.property(key))
                .filter(Optional::isPresent)
                .filter(property -> !property.get().getpType().equals(this.typeProperty.getpType()))
                .map(property -> new Property(property.get().getpType(), "raw", vertexProperty.value()))
                .toJavaOptional();
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private Ontology.Accessor ont;
    private Map<String, EEntityBase> eEntityBases;
    private Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> eRels;

    private com.yangdb.fuse.model.ontology.Property typeProperty;

    boolean includeEntities;
    boolean includeRelationships;
    //endregion
}
