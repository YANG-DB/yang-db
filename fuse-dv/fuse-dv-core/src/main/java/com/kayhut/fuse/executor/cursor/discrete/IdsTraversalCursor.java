package com.kayhut.fuse.executor.cursor.discrete;

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
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.*;

import static com.kayhut.fuse.model.results.AssignmentsQueryResult.Builder.instance;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class IdsTraversalCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new IdsTraversalCursor((TraversalCursorContext)context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public IdsTraversalCursor(TraversalCursorContext context) {
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

    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {

        return null;
    }
    //endregion

    //region Properties
    public TraversalCursorContext getContext() {
        return context;
    }
    //endregion

    //region Private Methods

    //endregion

    //region Fields
    private TraversalCursorContext context;
    private Ontology.Accessor ont;
    private Map<String, EEntityBase> eEntityBases;
    private Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> eRels;

    private com.kayhut.fuse.model.ontology.Property typeProperty;

    boolean includeEntities;
    boolean includeRelationships;
    //endregion
}
