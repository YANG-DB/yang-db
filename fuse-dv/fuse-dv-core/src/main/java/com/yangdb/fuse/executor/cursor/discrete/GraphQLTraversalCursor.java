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
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.logical.CompositeLogicalNode;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.results.*;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

import static com.yangdb.fuse.model.results.AssignmentsQueryResult.Builder.instance;


public class GraphQLTraversalCursor extends PathsTraversalCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new GraphQLTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public GraphQLTraversalCursor(TraversalCursorContext context) {
        super(context);
    }
    //endregion


    protected Assignment<LogicalNode, LogicalEdge> toAssignment(Path path) {
        Assignment.Builder builder = Assignment.Builder.instance();
        Assignment<LogicalNode, LogicalEdge> newAssignment = new Assignment<>();

        List<Object> pathObjects = path.objects();
        List<Set<String>> pathlabels = path.labels();
        for (int objectIndex = 0; objectIndex < pathObjects.size(); objectIndex++) {
            Object pathObject = pathObjects.get(objectIndex);
            String pathLabel = pathlabels.get(objectIndex).iterator().next();

            if (Vertex.class.isAssignableFrom(pathObject.getClass())) {
                builder.withEntity(toEntity((Vertex) pathObject, this.eEntityBases.get(pathLabel)));
            } else if (Edge.class.isAssignableFrom(pathObject.getClass())) {
                Tuple3<EEntityBase, Rel, EEntityBase> relTuple = this.eRels.get(pathLabel);
                //todo check is this "relation" has a property counterpart in the entity
            } else {
                throw new UnsupportedOperationException("unexpected object in path");
            }
        }

        return builder.build();
    }

    @Override
    protected com.yangdb.fuse.model.logical.Vertex toEntity(Vertex vertex, EEntityBase element) {
        String eType = vertex.label();
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();
        return new CompositeLogicalNode(vertex.id().toString(),eType)
                .withTag(element.geteTag())
                .withProperties(properties);

    }
}
