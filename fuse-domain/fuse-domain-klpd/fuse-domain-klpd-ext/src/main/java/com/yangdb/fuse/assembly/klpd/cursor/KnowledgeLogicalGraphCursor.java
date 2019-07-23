package com.yangdb.fuse.assembly.klpd.cursor;

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

import com.yangdb.fuse.assembly.klpd.KlpdRoutedSchemaProviderFactory;
import com.yangdb.fuse.assembly.klpd.KnowledgeLogicalGraphCursorRequest;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import com.yangdb.fuse.model.results.Relationship;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class KnowledgeLogicalGraphCursor extends KnowledgeGraphHierarchyTraversalCursor {

    private final KnowledgeLogicalGraphCursorRequest.GraphFormat format;

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new KnowledgeLogicalGraphCursor(
                    (TraversalCursorContext) context,
                    ((KnowledgeLogicalGraphCursorRequest) context.getCursorRequest()).getCountTags(),
                    ((KnowledgeLogicalGraphCursorRequest) context.getCursorRequest()).getFormat());
        }
        //endregion
    }

    public KnowledgeLogicalGraphCursor(TraversalCursorContext context, Iterable<String> countTags, KnowledgeLogicalGraphCursorRequest.GraphFormat format) {
        super(context, countTags);
        this.format = format;
    }


    @Override
    protected Assignment compose(Assignment.Builder builder) {
        Assignment<LogicalNode, LogicalEdge> newAssignment = new Assignment<>();
        Assignment<Entity, Relationship> assignment = builder.build();

        Map<String, LogicalEdge> edgeMap = assignment.getRelationships().stream()
                .filter(r -> r.getrType().equals(KlpdRoutedSchemaProviderFactory.LogicalTypes.RELATED_ENTITY))
                .map(r ->
                        new LogicalEdge(r.getrID(), r.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.CATEGORY).get().getValue().toString(),
                                r.geteID1(), r.geteID2(), r.isDirectional())
                                .withMetadata(r.getProperties())
                )
                .collect(Collectors.toMap(LogicalEdge::getId, p -> p));

        Map<String, LogicalNode> entityMap = assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(KlpdRoutedSchemaProviderFactory.LogicalTypes.ENTITY))
                .map(e ->
                        new LogicalNode(
                                e.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LOGICAL_ID).orElse(new Property(KlpdRoutedSchemaProviderFactory.SchemaFields.LOGICAL_ID, e.geteID())).getValue().toString(),
                                e.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.CATEGORY).orElse(new Property(KlpdRoutedSchemaProviderFactory.SchemaFields.CATEGORY, KlpdRoutedSchemaProviderFactory.SchemaFields.CATEGORY)).getValue().toString())
                                .withMetadata(e.getProperties())
                )
                .collect(Collectors.toMap(LogicalNode::getId, p -> p));

        assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(KlpdRoutedSchemaProviderFactory.LogicalTypes.EVALUE))
                .forEach(p -> {
                    if (p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LOGICAL_ID).isPresent() &&
                            entityMap.containsKey(p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LOGICAL_ID).get().getValue().toString())) {
                        //populate properties
                        entityMap.get(p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LOGICAL_ID).get().getValue().toString())
                                .withProperty(p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.FIELD_ID).get().getValue().toString(), value(p));

                    }
                });
        assignment.getEntities()
                .stream().filter(e -> e.geteType().equals(KlpdRoutedSchemaProviderFactory.LogicalTypes.RVALUE))
                .forEach(p -> {
                    if (p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.RELATION_ID).isPresent() &&
                            edgeMap.containsKey(p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.RELATION_ID).get().getValue().toString())) {
                        //populate properties
                        edgeMap.get(p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.RELATION_ID).get().getValue().toString())
                                .withProperty(p.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.RELATION_ID).get().getValue().toString(), value(p));

                    }
                });


        newAssignment.setEntities(new ArrayList<>(entityMap.values()));
        newAssignment.setRelationships(new ArrayList<>(edgeMap.values()));
        return newAssignment;
    }

    private Object value(Entity entity) {
        if ((entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.STRING_VALUE).isPresent() && entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.STRING_VALUE).get().getValue() != null))
            return entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.STRING_VALUE).get().getValue();
        if ((entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.INT_VALUE).isPresent() && entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.INT_VALUE).get().getValue() != null))
            return entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.INT_VALUE).get().getValue();
        if ((entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.DATE_VALUE).isPresent() && entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.DATE_VALUE).get().getValue() != null))
            return entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.DATE_VALUE).get().getValue();
        if ((entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LONG_VALUE).isPresent() && entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LONG_VALUE).get().getValue() != null))
            return entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.LONG_VALUE).get().getValue();
        if ((entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.FLOAT_VALUE).isPresent() && entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.FLOAT_VALUE).get().getValue() != null))
            return entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.FLOAT_VALUE).get().getValue();
        if ((entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.GEO_VALUE).isPresent() && entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.GEO_VALUE).get().getValue() != null))
            return entity.getProperty(KlpdRoutedSchemaProviderFactory.SchemaFields.GEO_VALUE).get().getValue();
        return null;
    }
}
