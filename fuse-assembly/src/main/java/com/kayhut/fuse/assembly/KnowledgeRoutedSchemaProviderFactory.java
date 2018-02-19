package com.kayhut.fuse.assembly;

import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class KnowledgeRoutedSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        if (!ontology.getOnt().equals("Knowledge")) {
            return null;
        }

        List<IndexPartitions.Partition> ePartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("e00000000", "e10000000", "e0"),
                new IndexPartitions.Partition.Range.Impl<>("e10000000", "e20000000", "e1"),
                new IndexPartitions.Partition.Range.Impl<>("e20000000", "e30000000", "e2"));

        List<IndexPartitions.Partition> relPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("r00000000", "r10000000", "rel0"),
                new IndexPartitions.Partition.Range.Impl<>("r10000000", "r20000000", "rel1"),
                new IndexPartitions.Partition.Range.Impl<>("r20000000", "r30000000", "rel2"));

        List<IndexPartitions.Partition> refPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("ref00000000", "ref10000000", "ref0"),
                new IndexPartitions.Partition.Range.Impl<>("ref10000000", "ref20000000", "ref1"),
                new IndexPartitions.Partition.Range.Impl<>("ref20000000", "ref30000000", "ref2"));

        List<IndexPartitions.Partition> iPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("i00000000", "i10000000", "i0"),
                new IndexPartitions.Partition.Range.Impl<>("i10000000", "i20000000", "i1"),
                new IndexPartitions.Partition.Range.Impl<>("i20000000", "i30000000", "i2"));

        IndexPartitions entityPartitions = new IndexPartitions.Impl("logicalId", ePartitions);
        IndexPartitions entityValuePartitions = new IndexPartitions.Impl("logicalId", ePartitions);

        IndexPartitions relationPartitions = new IndexPartitions.Impl("_id", relPartitions);
        IndexPartitions relationValuePartitions = new IndexPartitions.Impl("relationId", relPartitions);

        IndexPartitions referencePartitions = new IndexPartitions.Impl("_id", refPartitions);

        IndexPartitions insightPartitions = new IndexPartitions.Impl("_id", iPartitions);

        Iterable<GraphElementPropertySchema> metadataProperties = Arrays.asList(
                new GraphElementPropertySchema.Impl("creationUser", "string"),
                new GraphElementPropertySchema.Impl("creationTime", "date"),
                new GraphElementPropertySchema.Impl("lastUpdateUser", "string"),
                new GraphElementPropertySchema.Impl("lastUpdateTime", "date"),
                new GraphElementPropertySchema.Impl("deleteUser", "string"),
                new GraphElementPropertySchema.Impl("deleteTime", "date"),
                new GraphElementPropertySchema.Impl("authorization", "string"),
                new GraphElementPropertySchema.Impl("authorizationCount", "int")
        );

        Iterable<GraphRedundantPropertySchema> redundantMetadataProperties = Stream.ofAll(metadataProperties)
                .<GraphRedundantPropertySchema>map(propertySchema ->
                        new GraphRedundantPropertySchema.Impl(propertySchema.getName(), propertySchema.getName(), propertySchema.getType()))
                .toJavaList();

        Iterable<GraphRedundantPropertySchema> entityEdgeRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("category", "category", "string")))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> valueEdgeRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("fieldId", "fieldId", "string"),
                new GraphRedundantPropertySchema.Impl("bdt", "bdt", "string"),
                new GraphRedundantPropertySchema.Impl("stringValue", "stringValue", "string",
                        Arrays.asList(
                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("stringValue.keyword"),
                                new GraphElementPropertySchema.NgramsIndexingSchema.Impl("stringValue", 10))),
                new GraphRedundantPropertySchema.Impl("intValue", "intValue", "int"),
                new GraphRedundantPropertySchema.Impl("dateValue", "dateValue", "date")))
                .appendAll(redundantMetadataProperties).toJavaList();

        Iterable<GraphRedundantPropertySchema> relationDualRedundantProperties = Stream.<GraphRedundantPropertySchema>ofAll(Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("category", "category", "string")))
                .appendAll(redundantMetadataProperties).toJavaList();


        Iterable<GraphRedundantPropertySchema> relationEntityARedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl("category", "entityACategory", "string"),
                new GraphRedundantPropertySchema.Impl("logicalId", "entityALogicalId", "string")
        );

        Iterable<GraphRedundantPropertySchema> relationEntityBRedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl("category", "entityBCategory", "string"),
                new GraphRedundantPropertySchema.Impl("logicalId", "entityBLogicalId", "string")
        );

        return new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVirtualVertexSchema.Impl("LogicalEntity"),
                        new GraphVertexSchema.Impl(
                                "Entity",
                                new GraphElementConstraint.Impl(__.has(T.label, "entity")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("logicalId", "string")
                                )),
                                Optional.of(entityPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl("category", "string"),
                                        new GraphElementPropertySchema.Impl("logicalId", "string"),
                                        new GraphElementPropertySchema.Impl("context", "string")))
                                .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                "Evalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.value")),
                                Optional.empty(),
                                Optional.of(entityValuePartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl("logicalId", "string"),
                                        new GraphElementPropertySchema.Impl("context", "string"),
                                        new GraphElementPropertySchema.Impl("bdt", "string"),
                                        new GraphElementPropertySchema.Impl("fieldId", "string"),
                                        new GraphElementPropertySchema.Impl("intValue", "int"),
                                        new GraphElementPropertySchema.Impl("dateValue", "date"),
                                        new GraphElementPropertySchema.Impl("stringValue", "string",
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl("stringValue.keyword"),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl("stringValue", 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                "Relation",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.empty(),
                                Optional.of(relationPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl("category", "string"),
                                        new GraphElementPropertySchema.Impl("context", "string")))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                "Rvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "r.value")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("relationId", "string")
                                )),
                                Optional.of(relationPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl("context", "string"),
                                        new GraphElementPropertySchema.Impl("bdt", "string"),
                                        new GraphElementPropertySchema.Impl("fieldId", "string"),
                                        new GraphElementPropertySchema.Impl("intValue", "int"),
                                        new GraphElementPropertySchema.Impl("dateValue", "date"),
                                        new GraphElementPropertySchema.Impl("stringValue", "string",
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl("stringValue.keyword"),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl("stringValue", 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                "Reference",
                                new GraphElementConstraint.Impl(__.has(T.label, "reference")),
                                Optional.empty(),
                                Optional.of(referencePartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl("system", "string"),
                                        new GraphElementPropertySchema.Impl("title", "string",
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl("title.keyword"),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl("title", 10))),
                                        new GraphElementPropertySchema.Impl("value", "string",
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl("value.keyword"),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl("value", 10))),
                                        new GraphElementPropertySchema.Impl("url", "string",
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl("url.keyword"),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl("url", 10)))))
                                        .appendAll(metadataProperties).toJavaList()),
                        new GraphVertexSchema.Impl(
                                "Insight",
                                new GraphElementConstraint.Impl(__.has(T.label, "insight")),
                                Optional.empty(),
                                Optional.of(insightPartitions),
                                Stream.<GraphElementPropertySchema>ofAll(Arrays.asList(
                                        new GraphElementPropertySchema.Impl("context", "string"),
                                        new GraphElementPropertySchema.Impl("value", "string",
                                                Arrays.asList(
                                                        new GraphElementPropertySchema.ExactIndexingSchema.Impl("value.keyword"),
                                                        new GraphElementPropertySchema.NgramsIndexingSchema.Impl("value", 10)))))
                                        .appendAll(metadataProperties).toJavaList())),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                "hasEntity",
                                new GraphElementConstraint.Impl(__.has(T.label, "entity")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("logicalId"),
                                        Optional.of("LogicalEntity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Entity"),
                                        entityEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasEvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityId"),
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Evalue"),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasRvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "r.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("relationId"),
                                        Optional.of("Relation"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string")
                                        )),
                                        Optional.of(relationPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Rvalue"),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("relationId", "string")
                                        )),
                                        Optional.of(relationValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasRelation",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Arrays.asList("entityAId", "entityBId"),
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("relationId"),
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties)),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasRelation",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Arrays.asList("entityAId", "entityBId"),
                                        Optional.of("Entity"),
                                        Collections.emptyList())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasOutRelation",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "e.relation"), __.has("direction", Direction.OUT.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("relationId"),
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties)),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasOutRelation",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Entity"),
                                        relationEntityARedundantProperties)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Relation"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInRelation",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "e.relation"), __.has("direction", Direction.IN.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("relationId"),
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties)),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInRelation",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityBId"),
                                        Optional.of("Entity"),
                                        relationEntityBRedundantProperties)),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasEntityReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "entity")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("refs"),
                                        Optional.of("Reference"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(entityPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasEvalueReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Evalue"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("refs"),
                                        Optional.of("Reference"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(entityValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasRelationReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Relation"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string")
                                        )),
                                        Optional.of(relationPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("refs"),
                                        Optional.of("Reference"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(relationPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasRvalueReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "r.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Rvalue"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("relationId", "string")
                                        )),
                                        Optional.of(relationValuePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("refs"),
                                        Optional.of("Reference"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(relationValuePartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInsightReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "insight")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Insight"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(insightPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("refs"),
                                        Optional.of("Reference"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(insightPartitions))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA, endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInsight",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.insight")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityId"),
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("insightId"),
                                        Optional.of("Insight"),
                                        Collections.emptyList())),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInsight",
                                new GraphElementConstraint.Impl(__.has(T.label, "insight")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityIds"),
                                        Optional.of("Entity"),
                                        Collections.emptyList())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Insight"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
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
