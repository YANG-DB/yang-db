package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.structure.promise.PromiseEdge;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.results.QueryResult.Builder.instance;

/**
 * Created by Roman on 05/04/2017.
 */
public class PromiseTraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        return new PromiseTraversalCursor(traversalCursorContext);
    }
    //endregion
}
