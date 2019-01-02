package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.LogicalTypes.*;
import static com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.MetadataFields.*;
import static com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.SchemaFields.*;
import static com.kayhut.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.SchemaTypes.*;
import static com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;
import static com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endB;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class KnowledgeRoutedSchemaProviderFactory implements GraphElementSchemaProviderFactory {

    public static final String DATE = "date";
    public static final String INT = "int";
    public static final String SCORE = "score";
    public static final String FLOAT = "float";
    public static final String STRING = "string";


    public interface MetadataFields {
        String CREATION_USER = "creationUser";
        String CREATION_TIME = "creationTime";
        String LAST_UPDATE_USER = "lastUpdateUser";
        String LAST_UPDATE_TIME = "lastUpdateTime";
        String DELETE_USER = "deleteUser";
        String DELETE_TIME = "deleteTime";
        String AUTHORIZATION = "authorization";
        String AUTHORIZATION_COUNT = "authorizationCount";
    }

    public interface SchemaFields {
        //primary key ids
        String INSIGHT_ID = "insightId";
        String LOGICAL_ID = "logicalId";
        String FIELD_ID = "fieldId";
        String RELATION_ID = "relationId";
        String ENTITY_IDS = "entityIds";
        String ENTITY_A_ID = "entityAId";
        String ENTITY_B_ID = "entityBId";
        String ENTITY_ID = "entityId";
        String ENTITY_A_LOGICAL_ID = "entityALogicalId";
        String ENTITY_B_LOGICAL_ID = "entityBLogicalId";

        //property fields
        String STRING_VALUE = "stringValue";
        String SYSTEM = "system";
        String DIRECTION = "direction";
        String ENTITY_A_CATEGORY = "entityACategory";
        String ENTITY_B_CATEGORY = "entityBCategory";
        String CONTEXT = "context";
        String BDT = "bdt";
        String INT_VALUE = "intValue";
        String DATE_VALUE = "dateValue";
        String CATEGORY = "category";
        String NAME = "name";
        String DESCRIPTION = "description";
        String MIME_TYPE = "mimeType";
        String PATH = "path";
        String DISPLAY_NAME = "displayName";
        String TITLE = "title";
        String VALUE = "value";
        String URL = "url";
        String CONTENT = "content";
        String REFS = "refs";
    }

    public interface LogicalTypes {
        String LOGICAL_ENTITY = "LogicalEntity";
        String EFILE = "Efile";
        String EVALUE = "Evalue";
        String ENTITY = "Entity";
        String INSIGHT = "Insight";
        String REFERENCE = "Reference";
        String RVALUE = "Rvalue";
        String RELATION = "Relation";
        //relations
        String HAS_ENTITY = "hasEntity";
        String HAS_EVALUE = "hasEvalue";
        String HAS_OUT_RELATION = "hasOutRelation";
        String HAS_IN_RELATION = "hasInRelation";
        String HAS_EVALUE_REFERENCE = "hasEvalueReference";
        String HAS_ENTITY_REFERENCE = "hasEntityReference";
        String HAS_RVALUE_REFERENCE = "hasRvalueReference";
        String HAS_INSIGHT_REFERENCE = "hasInsightReference";
        String HAS_INSIGHT = "hasInsight";
        String HAS_RELATION_REFERENCE = "hasRelationReference";
        String HAS_EFILE_REFERENCE = "hasEfileReference";
        String HAS_RELATION = "hasRelation";
        String HAS_RVALUE = "hasRvalue";
        String HAS_EFILE = "hasEfile";
        String RELATED_ENTITY = "relatedEntity";
    }

    public interface SchemaTypes {
        String E_RELATION = "e.relation";
        String E_VALUE = "e.value";
        String E_FILE = "e.file";
        String E_INSIGHT = "e.insight";
        String R_VALUE = "r.value";

        String S_ENTITY = "entity";
        String S_RELATION = "relation";
        String S_INSIGHT = "insight";
        String S_REFERENCE = "reference";
    }

    public static final String KEYWORD = "keyword";
    public static final String ID = "_id";

    private RawSchema schema;
    //region GraphElementSchemaProviderFactory Implementation

    @Inject
    public KnowledgeRoutedSchemaProviderFactory(RawSchema schema) {
        this.schema = schema;
    }

    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        if (!ontology.getOnt().equals("Knowledge")) {
            return null;
        }
        IndexPartitions entityPartitions = schema.getPartition(SchemaTypes.S_ENTITY);
        IndexPartitions entityValuePartitions = schema.getPartition(E_VALUE);
        IndexPartitions relationPartitions = schema.getPartition(S_RELATION);
        IndexPartitions entityFilePartitions = schema.getPartition(E_FILE);
        IndexPartitions relationValuePartitions = schema.getPartition(R_VALUE);
        IndexPartitions referencePartitions = schema.getPartition(SchemaTypes.S_REFERENCE);
        IndexPartitions insightPartitions = schema.getPartition(SchemaTypes.S_INSIGHT);

        Iterable<GraphElementPropertySchema> metadataProperties = Arrays.asList(
                new GraphElementPropertySchema.Impl(CREATION_USER, STRING),
                new GraphElementPropertySchema.Impl(CREATION_TIME, DATE),
                new GraphElementPropertySchema.Impl(LAST_UPDATE_USER, STRING),
                new GraphElementPropertySchema.Impl(LAST_UPDATE_TIME, DATE),
                new GraphElementPropertySchema.Impl(DELETE_USER, STRING),
                new GraphElementPropertySchema.Impl(DELETE_TIME, DATE),
                new GraphElementPropertySchema.Impl(AUTHORIZATION, STRING),
                new GraphElementPropertySchema.Impl(AUTHORIZATION_COUNT, INT),
                new GraphElementPropertySchema.Impl(SCORE, FLOAT),
                new GraphElementPropertySchema.Impl(REFS, STRING)
        );

        Iterable<GraphRedundantPropertySchema> redundantMetadataProperties = Stream.ofAll(metadataProperties)
                .<GraphRedundantPropertySchema>map(propertySchema ->
                        new GraphRedundantPropertySchema.Impl(propertySchema.getName(), propertySchema.getName(), propertySchema.getType()))
                .toJavaList();

        Iterable<GraphRedundantPropertySchema> entityEdgeRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, LOGICAL_ID, STRING),
                new GraphRedundantPropertySchema.Impl(CONTEXT, CONTEXT, STRING),
                new GraphRedundantPropertySchema.Impl(CATEGORY, CATEGORY, STRING)))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> valueEdgeRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, LOGICAL_ID, STRING),
                new GraphRedundantPropertySchema.Impl(CONTEXT, CONTEXT, STRING),
                new GraphRedundantPropertySchema.Impl(FIELD_ID, FIELD_ID, STRING),
                new GraphRedundantPropertySchema.Impl(BDT, BDT, STRING),
                new GraphRedundantPropertySchema.Impl(STRING_VALUE, STRING_VALUE, STRING,
                        Arrays.asList(
                                new GraphElementPropertySchema.ExactIndexingSchema.Impl(STRING_VALUE + "." + KEYWORD),
                                new GraphElementPropertySchema.NgramsIndexingSchema.Impl(STRING_VALUE, 10))),
                new GraphRedundantPropertySchema.Impl(INT_VALUE, INT_VALUE, INT),
                new GraphRedundantPropertySchema.Impl(DATE_VALUE, DATE_VALUE, DATE)))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> fileEdgeRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, LOGICAL_ID, STRING),
                new GraphRedundantPropertySchema.Impl(CONTEXT, CONTEXT, STRING),
                new GraphRedundantPropertySchema.Impl(CATEGORY, CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(NAME, NAME, STRING),
                new GraphRedundantPropertySchema.Impl(DESCRIPTION, DESCRIPTION, STRING,
                        Arrays.asList(
                                new GraphElementPropertySchema.ExactIndexingSchema.Impl(DESCRIPTION + "." + KEYWORD),
                                new GraphElementPropertySchema.NgramsIndexingSchema.Impl(DESCRIPTION, 10))),
                new GraphRedundantPropertySchema.Impl(MIME_TYPE, MIME_TYPE, STRING),
                new GraphRedundantPropertySchema.Impl(PATH, PATH, STRING),
                new GraphRedundantPropertySchema.Impl(DISPLAY_NAME, DISPLAY_NAME, STRING)))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> relationDualRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, LOGICAL_ID, STRING),
                new GraphRedundantPropertySchema.Impl(CONTEXT, CONTEXT, STRING),
                new GraphRedundantPropertySchema.Impl(CATEGORY, CATEGORY, STRING)))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> relationOutDualRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, LOGICAL_ID, STRING),
                new GraphRedundantPropertySchema.Impl(CONTEXT, CONTEXT, STRING),
                new GraphRedundantPropertySchema.Impl(CATEGORY, CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_A_ID, ENTITY_A_ID, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_A_CATEGORY, ENTITY_A_CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_B_ID, ENTITY_B_ID, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_B_CATEGORY, ENTITY_B_CATEGORY, STRING)))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> relationInDualRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, LOGICAL_ID, STRING),
                new GraphRedundantPropertySchema.Impl(CONTEXT, CONTEXT, STRING),
                new GraphRedundantPropertySchema.Impl(CATEGORY, CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_A_ID, ENTITY_B_ID, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_A_CATEGORY, ENTITY_B_CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_B_ID, ENTITY_A_ID, STRING),
                new GraphRedundantPropertySchema.Impl(ENTITY_B_CATEGORY, ENTITY_A_CATEGORY, STRING)))
                .appendAll(redundantMetadataProperties).toJavaList();


        Iterable<GraphRedundantPropertySchema> relationEntityARedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl(CATEGORY, ENTITY_A_CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, ENTITY_A_LOGICAL_ID, STRING)
        );

        Iterable<GraphRedundantPropertySchema> relationEntityBRedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl(CATEGORY, ENTITY_B_CATEGORY, STRING),
                new GraphRedundantPropertySchema.Impl(LOGICAL_ID, ENTITY_B_LOGICAL_ID, STRING)
        );

        return new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVirtualVertexSchema.Impl(LOGICAL_ENTITY),
                        new GraphVertexSchema.Impl(
                                LogicalTypes.ENTITY,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_ENTITY)),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                )),
                                Optional.of(entityPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(CATEGORY, STRING),
                                        new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING),
                                        new GraphElementPropertySchema.Impl(CONTEXT, STRING)))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                EFILE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_FILE)),
                                Optional.empty(),
                                Optional.of(entityFilePartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(ENTITY_ID, STRING),
                                        new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING),
                                        new GraphElementPropertySchema.Impl(CONTEXT, STRING),
                                        new GraphElementPropertySchema.Impl(CATEGORY, STRING),
                                        new GraphElementPropertySchema.Impl(PATH, STRING),
                                        new GraphElementPropertySchema.Impl(NAME, STRING),
                                        new GraphElementPropertySchema.Impl(DISPLAY_NAME, STRING),
                                        new GraphElementPropertySchema.Impl(MIME_TYPE, STRING),
                                        new GraphElementPropertySchema.Impl(DESCRIPTION, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(DESCRIPTION + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(DESCRIPTION, 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                EVALUE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_VALUE)),
                                Optional.empty(),
                                Optional.of(entityValuePartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING),
                                        new GraphElementPropertySchema.Impl(CONTEXT, STRING),
                                        new GraphElementPropertySchema.Impl(BDT, STRING),
                                        new GraphElementPropertySchema.Impl(FIELD_ID, STRING),
                                        new GraphElementPropertySchema.Impl(INT_VALUE, INT),
                                        new GraphElementPropertySchema.Impl(DATE_VALUE, DATE),
                                        new GraphElementPropertySchema.Impl(STRING_VALUE, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(STRING_VALUE + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(STRING_VALUE, 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                RELATION,
                                new GraphElementConstraint.Impl(__.start().has(T.label, S_RELATION)),
                                Optional.empty(),
                                Optional.of(relationPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(CATEGORY, STRING),
                                        new GraphElementPropertySchema.Impl(CONTEXT, STRING)))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                RVALUE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, R_VALUE)),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl(RELATION_ID, STRING)
                                )),
                                Optional.of(relationPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(CONTEXT, STRING),
                                        new GraphElementPropertySchema.Impl(BDT, STRING),
                                        new GraphElementPropertySchema.Impl(FIELD_ID, STRING),
                                        new GraphElementPropertySchema.Impl(INT_VALUE, INT),
                                        new GraphElementPropertySchema.Impl(DATE_VALUE, DATE),
                                        new GraphElementPropertySchema.Impl(STRING_VALUE, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(STRING_VALUE + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(STRING_VALUE, 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                LogicalTypes.REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_REFERENCE)),
                                Optional.empty(),
                                Optional.of(referencePartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(SYSTEM, STRING),
                                        new GraphElementPropertySchema.Impl(TITLE, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(TITLE + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(TITLE, 10))),
                                        new GraphElementPropertySchema.Impl(VALUE, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(VALUE + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(VALUE, 10))),
                                        new GraphElementPropertySchema.Impl(CONTENT, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(CONTENT + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(CONTENT, 10))),
                                        new GraphElementPropertySchema.Impl(URL, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(URL + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(URL, 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                LogicalTypes.INSIGHT,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_INSIGHT)),
                                Optional.empty(),
                                Optional.of(insightPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl(CONTEXT, STRING),
                                        new GraphElementPropertySchema.Impl(CONTENT, STRING,
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl(CONTENT + "." + KEYWORD),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl(CONTENT, 10)))))
                                        .appendAll(metadataProperties).toJavaList())),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                HAS_ENTITY,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_ENTITY)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(LOGICAL_ID),
                                        Optional.of(LOGICAL_ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        entityEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                HAS_EVALUE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_VALUE)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(EVALUE),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                HAS_EFILE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_FILE)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(EFILE),
                                        fileEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityFilePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                HAS_RVALUE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, R_VALUE)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(RELATION_ID),
                                        Optional.of(RELATION),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING)
                                        )),
                                        Optional.of(relationPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(RVALUE),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(RELATION_ID, STRING)
                                        )),
                                        Optional.of(relationValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                HAS_RELATION,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_RELATION)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Arrays.asList(ENTITY_A_ID, ENTITY_B_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(RELATION_ID),
                                        Optional.of(RELATION),
                                        relationDualRedundantProperties)),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_RELATION,
                                new GraphElementConstraint.Impl(__.start().has(T.label, S_RELATION)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Arrays.asList(ENTITY_A_ID, ENTITY_B_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(RELATION),
                                        relationDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING))),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_OUT_RELATION,
                                new GraphElementConstraint.Impl(__.start().and(__.start().has(T.label, E_RELATION), __.start().has(DIRECTION, Direction.OUT.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_A_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(RELATION_ID),
                                        Optional.of(RELATION),
                                        relationOutDualRedundantProperties)),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_OUT_RELATION,
                                new GraphElementConstraint.Impl(__.start().has(T.label, S_RELATION)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_A_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        relationEntityARedundantProperties)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(RELATION),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING))),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                RELATED_ENTITY,
                                new GraphElementConstraint.Impl(__.start().and(__.start().has(T.label, E_RELATION))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_A_ID),
                                        Optional.of(ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_B_ID),
                                        Optional.of(ENTITY))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                RELATED_ENTITY,
                                new GraphElementConstraint.Impl(__.start().and(__.start().has(T.label, E_RELATION))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_B_ID),
                                        Optional.of(ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_A_ID),
                                        Optional.of(ENTITY))),
                                Direction.IN,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_IN_RELATION,
                                new GraphElementConstraint.Impl(__.start().and(__.start().has(T.label, E_RELATION), __.start().has(DIRECTION, Direction.IN.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_A_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(RELATION_ID),
                                        Optional.of(RELATION),
                                        relationInDualRedundantProperties)),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_IN_RELATION,
                                new GraphElementConstraint.Impl(__.start().has(T.label, S_RELATION)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_B_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        relationEntityBRedundantProperties)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(RELATION),
                                        relationInDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING))),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_ENTITY_REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_ENTITY)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(REFS),
                                        Optional.of(LogicalTypes.REFERENCE),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(referencePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_EVALUE_REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_VALUE)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(EVALUE),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(REFS),
                                        Optional.of(LogicalTypes.REFERENCE),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(referencePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_EFILE_REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_FILE)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(EFILE),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityFilePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(REFS),
                                        Optional.of(LogicalTypes.REFERENCE),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(referencePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_RELATION_REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, S_RELATION)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(RELATION),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING)
                                        )),
                                        Optional.of(relationPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(REFS),
                                        Optional.of(LogicalTypes.REFERENCE),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(referencePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_RVALUE_REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, R_VALUE)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(RVALUE),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(RELATION_ID, STRING)
                                        )),
                                        Optional.of(relationValuePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(REFS),
                                        Optional.of(LogicalTypes.REFERENCE),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(referencePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_INSIGHT_REFERENCE,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_INSIGHT)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(LogicalTypes.INSIGHT),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(insightPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(REFS),
                                        Optional.of(LogicalTypes.REFERENCE),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(referencePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_INSIGHT,
                                new GraphElementConstraint.Impl(__.start().has(T.label, E_INSIGHT)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_ID),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(LOGICAL_ID, STRING)
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(INSIGHT_ID),
                                        Optional.of(LogicalTypes.INSIGHT),
                                        Collections.emptyList())),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                HAS_INSIGHT,
                                new GraphElementConstraint.Impl(__.start().has(T.label, SchemaTypes.S_INSIGHT)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ENTITY_IDS),
                                        Optional.of(LogicalTypes.ENTITY),
                                        Collections.emptyList())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(ID),
                                        Optional.of(LogicalTypes.INSIGHT),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl(ID, STRING))),
                                        Optional.of(insightPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()))
        );
    }
    //endregion
}
