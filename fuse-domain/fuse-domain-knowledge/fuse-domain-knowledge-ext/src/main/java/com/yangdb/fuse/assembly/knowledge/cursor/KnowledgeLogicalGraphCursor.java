package com.yangdb.fuse.assembly.knowledge.cursor;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import com.yangdb.fuse.model.results.Relationship;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.LogicalTypes.*;
import static com.yangdb.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.SchemaFields.*;

public class KnowledgeLogicalGraphCursor extends KnowledgeGraphHierarchyTraversalCursor {

    private final LogicalGraphCursorRequest.GraphFormat format;

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new KnowledgeLogicalGraphCursor(
                    (TraversalCursorContext) context,
                    ((LogicalGraphCursorRequest) context.getCursorRequest()).getCountTags(),
                    ((LogicalGraphCursorRequest) context.getCursorRequest()).getFormat());
        }
        //endregion
    }

    public KnowledgeLogicalGraphCursor(TraversalCursorContext context, Iterable<String> countTags, LogicalGraphCursorRequest.GraphFormat format) {
        super(context, countTags);
        this.format = format;
    }

    @Override
    protected Assignment compose(Assignment.Builder builder) {
        Assignment<LogicalNode, LogicalEdge> newAssignment = new Assignment<>();
        Assignment<Entity, Relationship> assignment = builder.build();

        Map<String, LogicalEdge> edgeMap = assignment.getRelationships().stream()
                .filter(r -> r.getrType().equals(RELATED_ENTITY))
                .map(r ->
                        new LogicalEdge(r.id(), r.label(),
                                r.source(), r.target(), r.isDirectional())
                                .withMetadata(r.getProperties())
                )
                .collect(Collectors.toMap(LogicalEdge::id, p -> p));

        Map<String, LogicalNode> entityMap = assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(ENTITY))
                .map(e ->
                        new LogicalNode(
                                e.id(),
                                e.label())
                                .withMetadata(e.getProperties())
                )
                .collect(Collectors.toMap(LogicalNode::getId, p -> p));

        assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(EVALUE))
                .forEach(p -> {
                    if (entityMap.containsKey(fieldId(p))) {
                        //populate properties
                        entityMap.get(fieldId(p))
                                .withProperty(p.getProperty(FIELD_ID).get().getValue().toString(), value(p));

                    }
                });
        assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(RVALUE))
                .forEach(p -> {
                    if (edgeMap.containsKey(p.id())) {
                        //populate properties
                        edgeMap.get(p.id())
                                .withProperty(p.id(), value(p));

                    }
                });


        newAssignment.setEntities(new ArrayList<>(entityMap.values()));
        newAssignment.setRelationships(new ArrayList<>(edgeMap.values()));
        return newAssignment;
    }

    private String fieldId(Entity p) {
        return String.format("%s.%s", p.getProperty(LOGICAL_ID).get().getValue().toString(), p.getProperty(CONTEXT).get().getValue().toString());
    }

    private Object value(Entity entity) {
        if ((entity.getProperty(STRING_VALUE).isPresent() && entity.getProperty(STRING_VALUE).get().getValue() != null))
            return entity.getProperty(STRING_VALUE).get().getValue();
        if ((entity.getProperty(INT_VALUE).isPresent() && entity.getProperty(INT_VALUE).get().getValue() != null))
            return entity.getProperty(INT_VALUE).get().getValue();
        if ((entity.getProperty(DATE_VALUE).isPresent() && entity.getProperty(DATE_VALUE).get().getValue() != null))
            return entity.getProperty(DATE_VALUE).get().getValue();
        if ((entity.getProperty(LONG_VALUE).isPresent() && entity.getProperty(LONG_VALUE).get().getValue() != null))
            return entity.getProperty(LONG_VALUE).get().getValue();
        if ((entity.getProperty(FLOAT_VALUE).isPresent() && entity.getProperty(FLOAT_VALUE).get().getValue() != null))
            return entity.getProperty(FLOAT_VALUE).get().getValue();
        if ((entity.getProperty(GEO_VALUE).isPresent() && entity.getProperty(GEO_VALUE).get().getValue() != null))
            return entity.getProperty(GEO_VALUE).get().getValue();
        return null;
    }
}
