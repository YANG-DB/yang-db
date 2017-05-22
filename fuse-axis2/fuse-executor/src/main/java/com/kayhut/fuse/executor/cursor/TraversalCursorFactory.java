package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
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
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.structure.PromiseEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.model.results.QueryResult.Builder.instance;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        return new TraversalCursor(traversalCursorContext);
    }
    //endregion

    /**
     * Created by liorp on 3/20/2017.
     */
    public static class TraversalCursor implements Cursor {

        public TraversalCursor(TraversalCursorContext context) {
            this.context = context;
        }
        //endregion

        //region Cursor Implementation
        @Override
        public QueryResult getNextResults(int numResults) {
            return toQuery(numResults, context);
        }
        //endregion

        //region Properties
        public TraversalCursorContext getContext() {
            return context;
        }
        //endregion

        //region Private Methods
        private QueryResult toQuery(int numResults, TraversalCursorFactory.TraversalCursorContext context) {
            QueryResult.Builder builder = instance();
            builder.withPattern(context.getQueryResource().getQuery());
            //build assignments
            (context.getTraversal().next(numResults)).forEach(path -> {
                builder.withAssignment(toAssignment(context, path));
            });
            return builder.build();
        }

        private Assignment toAssignment(TraversalCursorFactory.TraversalCursorContext context, Path path) {
            Assignment.Builder builder = Assignment.Builder.instance();
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
                            PlanUtil.prev(context.getQueryResource().getExecutionPlan().getPlan(), planOp, EntityOp.class);
                    Optional<EntityOp> nextEntityOp =
                            PlanUtil.next(context.getQueryResource().getExecutionPlan().getPlan(), planOp, EntityOp.class);

                    builder.withRelationship(toRelationship(path,
                            prevEntityOp.get().getAsgEBase().geteBase(),
                            relationOp.getAsgEBase().geteBase(),
                            nextEntityOp.get().getAsgEBase().geteBase()));
                }
            });

            return builder.build();
        }

        private Entity toEntity(TraversalCursorFactory.TraversalCursorContext context, Path path, EUntyped element) {
            PromiseVertex vertex = path.get(element.geteTag());
            IdPromise idPromise = (IdPromise)vertex.getPromise();

            int eType = idPromise.getLabel().isPresent() ?
                    OntologyUtil.getEntityTypeIdByName(context.getOntology(), idPromise.getLabel().get()) :
                    0;

            return toEntity(vertex.id().toString(),eType,element.geteTag());
        }

        private Entity toEntity(Path path, EConcrete element) {
            PromiseVertex vertex = path.get(element.geteTag());
            return toEntity(vertex.id().toString(),element.geteType(),element.geteTag());
        }

        private Entity toEntity(Path path, ETyped element) {
            PromiseVertex vertex = path.get(element.geteTag());
            return toEntity(vertex.id().toString(),element.geteType(),element.geteTag());
        }

        private Entity toEntity(String eId,int eType, String eTag ) {
            Entity.Builder builder = Entity.Builder.instance();
            builder.withEID(eId);
            builder.withEType(eType);
            builder.withETag(Collections.singletonList(eTag));
            return builder.build();
        }

        private Relationship toRelationship(Path path, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
            Relationship.Builder builder = Relationship.Builder.instance();
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
        //endregion

        //region Fields
        private TraversalCursorContext context;
        //endregion
    }

    /**
     * Created by Roman on 05/04/2017.
     */
    public static class TraversalCursorContext implements Context {
        //region Constructor
        public TraversalCursorContext(Ontology ontology, QueryResource queryResource, Traversal<Element, Path> traversal) {
            this.ontology = ontology;
            this.queryResource = queryResource;
            this.traversal = traversal;
        }
        //endregion

        //region CursorFactory.Context Implementation
        @Override
        public QueryResource getQueryResource() {
            return this.queryResource;
        }
        //endregion

        //region Properties
        public Traversal<Element, Path> getTraversal() {
            return this.traversal;
        }

        public Ontology getOntology() {
            return ontology;
        }
        //endregion

        private Ontology ontology;
        //region Fields
        private QueryResource queryResource;
        private Traversal<Element, Path> traversal;
        //endregion
    }
}
