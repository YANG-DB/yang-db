package com.kayhut.fuse.executor.translation;

import com.kayhut.fuse.executor.cursor.TraversalCursorFactory;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
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
import java.util.List;
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
        context.getQueryResource().getQuery().getElements().forEach(element-> {
            if(element instanceof ETyped) {
                builder.withEntity(toEntity(path, (ETyped) element));
            }
            if(element instanceof EConcrete) {
                builder.withEntity(toEntity(path, (EConcrete) element));
            }
            if(element instanceof EUntyped) {
                builder.withEntity(toEntity(context, path, (EUntyped) element));
            }
            if(element instanceof Rel) {
                builder.withRelationship(toRelationship(path, (Rel) element));
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
        Entity.EntityBuilder builder = Entity.EntityBuilder.anEntity();
        builder.withEID(eId);
        builder.withEType(eType);
        builder.withETag(Collections.singletonList(eTag));
        return builder.build();
    }

    static Relationship toRelationship(Path path, Rel element) {
        Relationship.RelationshipBuilder builder = Relationship.RelationshipBuilder.aRelationship();
        PromiseEdge edge = path.get(element.getrType());
        builder.withRID(edge.inVertex().id().toString()+"->"+edge.outVertex().id().toString());
        builder.withEID1(edge.inVertex().id().toString());
        builder.withEID2(edge.outVertex().id().toString());
        builder.withRType(element.getrType());
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
