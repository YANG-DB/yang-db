package com.kayhut.fuse.executor.translation;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.executor.cursor.TraversalCursorFactory;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.results.Relationship;
import com.kayhut.fuse.unipop.controller.utils.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.PromiseEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.kayhut.fuse.model.results.QueryResult.QueryResultBuilder.aQueryResult;

/**
 * Created by liorp on 4/6/2017.
 */
public interface TraversalCursorQueryResultsTranslator {
    /**
     * translatePath traversal to query results
     * @return
     */
    static QueryResult translatePath(int numResults, TraversalCursorFactory.TraversalCursorContext context) {
        QueryResult.QueryResultBuilder builder = aQueryResult();
        builder.withPattern(context.getQueryResource().getQuery());
        //build assignments
        (context.getTraversal().next(numResults)).forEach(path -> {
            builder.withAssignment(toAssignment(context, path));
        });
        return builder.build();
    }

    static Assignment toAssignment(TraversalCursorFactory.TraversalCursorContext context, Path path) {
        Assignment.AssignmentBuilder builder = Assignment.AssignmentBuilder.anAssignment();
        context.getQueryResource().getExecutionPlan().getPlan().getOps().forEach(planOp -> {
            if (planOp instanceof EntityOp) {
                EEntityBase entity = ((EntityOp)planOp).getAsgEBase().geteBase();

                if(entity instanceof EConcrete) {
                    builder.withEntity(toEntity(path, (EConcrete) entity));
                } else if(entity instanceof ETyped) {
                    builder.withEntity(toEntity(path, (ETyped) entity));
                } else if(entity instanceof EUntyped) {
                    builder.withEntity(toEntity(context, path, (EUntyped) entity));
                }
            } else if (planOp instanceof RelationOp) {
                RelationOp relationOp = (RelationOp)planOp;
                Optional<EntityOp> prevEntityOp =
                        PlanUtil.getPrev(context.getQueryResource().getExecutionPlan().getPlan(), planOp, EntityOp.class);
                Optional<EntityOp> nextEntityOp =
                        PlanUtil.getNext(context.getQueryResource().getExecutionPlan().getPlan(), planOp, EntityOp.class);

                builder.withRelationship(toRelationship(path,
                        prevEntityOp.get().getAsgEBase().geteBase(),
                        relationOp.getAsgEBase().geteBase(),
                        nextEntityOp.get().getAsgEBase().geteBase()));
            }
        });

        return builder.build();
    }

    static Entity toEntity(TraversalCursorFactory.TraversalCursorContext context, Path path, EUntyped element) {
        PromiseVertex vertex = path.get(element.geteTag());
        Set key = new TraversalValuesByKeyProvider().getValueByKey(((TraversalConstraint) vertex.getConstraint().get()).getTraversal(), T.label.getAccessor());
        int eType = OntologyUtil.getEntityTypeIdByName(context.getOntology(), key.iterator().next().toString());
        return toEntity(vertex.id().toString(),eType,element.geteTag());
    }

    static Entity toEntity(Path path, EConcrete element) {
        PromiseVertex vertex = path.get(element.geteTag());
        return toEntity(vertex.id().toString(),element.geteType(),element.geteTag());
    }

    static Entity toEntity(Path path, ETyped element) {
        PromiseVertex vertex = path.get(element.geteTag());
        return toEntity(vertex.id().toString(),element.geteType(),element.geteTag());
    }

    static Entity toEntity(String eId,int eType, String eTag ) {
        Entity.Builder builder = Entity.Builder.anEntity();
        builder.withEID(eId);
        builder.withEType(eType);
        builder.withETag(Collections.singletonList(eTag));
        return builder.build();
    }

    static Relationship toRelationship(Path path, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.aRelationship();
        PromiseEdge edge = path.get(prevEntity.geteTag() + "-->" + nextEntity.geteTag());
        builder.withRID(edge.id().toString());
        builder.withRType(rel.getrType());

        switch (rel.getDir()) {
            case R:
                builder.withEID1(edge.outVertex().id().toString());
                builder.withEID2(edge.inVertex().id().toString());
                builder.withETag1(prevEntity.geteTag());
                builder.withETag2(nextEntity.geteTag());
                break;

            case L:
                builder.withEID1(edge.inVertex().id().toString());
                builder.withEID2(edge.outVertex().id().toString());
                builder.withETag1(nextEntity.geteTag());
                builder.withETag2(prevEntity.geteTag());
        }

        return builder.build();
    }

    /**
     * translateGraph traversal to query results
     * @return
     */
    static QueryResult translateGraph(TraversalCursorFactory.TraversalCursorContext context) {
        //todo implement
        return aQueryResult().build();
    }

}
