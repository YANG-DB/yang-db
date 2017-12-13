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

        Iterable<GraphRedundantPropertySchema> valueEdgeRedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("security1", "security1", "string"),
                new GraphRedundantPropertySchema.Impl("security2", "security2", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateUser", "lastUpdateUser", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateTime", "lastUpdateTime", "date"),
                new GraphRedundantPropertySchema.Impl("creationUser", "creationUser", "string"),
                new GraphRedundantPropertySchema.Impl("creationTime", "creationTime", "date"),
                new GraphRedundantPropertySchema.Impl("deleteUser", "deleteUser", "string"),
                new GraphRedundantPropertySchema.Impl("deleteTime", "deleteTime", "date"),
                new GraphRedundantPropertySchema.Impl("fieldId", "fieldId", "string"),
                new GraphRedundantPropertySchema.Impl("bdt", "bdt", "string"),
                new GraphRedundantPropertySchema.Impl("stringValue", "stringValue", "string"),
                new GraphRedundantPropertySchema.Impl("textValue", "textValue", "string"),
                new GraphRedundantPropertySchema.Impl("intValue", "intValue", "int"),
                new GraphRedundantPropertySchema.Impl("dateValue", "dateValue", "date"));

        Iterable<GraphRedundantPropertySchema> relationDualRedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("category", "category", "string"),
                new GraphRedundantPropertySchema.Impl("security1", "security1", "string"),
                new GraphRedundantPropertySchema.Impl("security2", "security2", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateUser", "lastUpdateUser", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateTime", "lastUpdateTime", "date"),
                new GraphRedundantPropertySchema.Impl("creationUser", "creationUser", "string"),
                new GraphRedundantPropertySchema.Impl("creationTime", "creationTime", "date"),
                new GraphRedundantPropertySchema.Impl("deleteUser", "deleteUser", "string"),
                new GraphRedundantPropertySchema.Impl("deleteTime", "deleteTime", "date"));

        return new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl(
                                "Entity",
                                new GraphElementConstraint.Impl(__.has(T.label, "entity")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("logicalId", "string")
                                )),
                                Optional.of(entityPartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Evalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.value")),
                                Optional.empty(),
                                Optional.of(entityValuePartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Relation",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.empty(),
                                Optional.of(relationPartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Rvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "r.value")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("relationId", "string")
                                )),
                                Optional.of(relationPartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Reference",
                                new GraphElementConstraint.Impl(__.has(T.label, "reference")),
                                Optional.empty(),
                                Optional.of(referencePartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Insight",
                                new GraphElementConstraint.Impl(__.has(T.label, "insight")),
                                Optional.empty(),
                                Optional.of(insightPartitions),
                                Collections.emptyList())),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                "hasEvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityId",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Evalue"),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasRvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "r.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "relationId",
                                        Optional.of("Relation"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string")
                                        )),
                                        Optional.of(relationPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Rvalue"),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("relationId", "string")
                                        )),
                                        Optional.of(relationValuePartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasRelation",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("relationId", Optional.of("Relation"), relationDualRedundantProperties)),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasOutRelation",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "e.relation"), __.has("direction", Direction.OUT.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("relationId", Optional.of("Relation"), relationDualRedundantProperties)),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInRelation",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "e.relation"), __.has("direction", Direction.IN.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("relationId", Optional.of("Relation"), relationDualRedundantProperties)),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "entity")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("refs", Optional.of("Reference"), Collections.emptyList())),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Evalue"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityValuePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("refs", Optional.of("Reference"), Collections.emptyList())),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "relation")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Relation"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string")
                                        )),
                                        Optional.of(relationPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("refs", Optional.of("Reference"), Collections.emptyList())),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "r.value")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Rvalue"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("relationId", "string")
                                        )),
                                        Optional.of(relationValuePartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("refs", Optional.of("Reference"), Collections.emptyList())),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasReference",
                                new GraphElementConstraint.Impl(__.has(T.label, "insight")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Insight"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(insightPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("refs", Optional.of("Reference"), Collections.emptyList())),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInsight",
                                new GraphElementConstraint.Impl(__.has(T.label, "e.insight")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityId",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl("insightId", Optional.of("Insight"), Collections.emptyList())),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()))
                );
    }
    //endregion
}
