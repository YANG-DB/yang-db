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

        IndexPartitions entityPartitions = new IndexPartitions.Impl("logicalId",
                new IndexPartitions.Partition.Range.Impl<>("e000", "e300", "e0"),
                new IndexPartitions.Partition.Range.Impl<>("e300", "e600", "e1"),
                new IndexPartitions.Partition.Range.Impl<>("e600", "e999", "e2"));

        IndexPartitions relationPartitions = new IndexPartitions.Impl("relationId",
                new IndexPartitions.Partition.Range.Impl<>("r0000", "r1000", "r0"),
                new IndexPartitions.Partition.Range.Impl<>("r1000", "r2000", "r1"),
                new IndexPartitions.Partition.Range.Impl<>("r2000", "r9999", "r2"));

        Iterable<GraphRedundantPropertySchema> valueEdgeRedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("security1", "security1", "string"),
                new GraphRedundantPropertySchema.Impl("security2", "security2", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateUser", "lastUpdateUser", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateTime", "lastUpdateTime", "date"),
                new GraphRedundantPropertySchema.Impl("creationTime", "creationTime", "date"),
                new GraphRedundantPropertySchema.Impl("propertyId", "propertyId", "string"),
                new GraphRedundantPropertySchema.Impl("bdt", "bdt", "string"),
                new GraphRedundantPropertySchema.Impl("stringValue", "stringValue", "string"),
                new GraphRedundantPropertySchema.Impl("textValue", "textValue", "string"),
                new GraphRedundantPropertySchema.Impl("intValue", "intValue", "int"),
                new GraphRedundantPropertySchema.Impl("dateValue", "dateValue", "date"));

        Iterable<GraphRedundantPropertySchema> relationDualRedundantProperties = Arrays.asList(
                new GraphRedundantPropertySchema.Impl("logicalId", "logicalId", "string"),
                new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                new GraphRedundantPropertySchema.Impl("security1", "security1", "string"),
                new GraphRedundantPropertySchema.Impl("security2", "security2", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateUser", "lastUpdateUser", "string"),
                new GraphRedundantPropertySchema.Impl("lastUpdateTime", "lastUpdateTime", "date"),
                new GraphRedundantPropertySchema.Impl("creationTime", "creationTime", "date"));

        return new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl(
                                "Entity",
                                new GraphElementConstraint.Impl(__.has(T.label, "Entity")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("logicalId", "string")
                                )),
                                Optional.of(entityPartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Evalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "Evalue")),
                                Optional.empty(),
                                Optional.of(entityPartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Relation",
                                new GraphElementConstraint.Impl(__.has(T.label, "Relation.S")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("relationId", "string")
                                )),
                                Optional.of(relationPartitions),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Rvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "Rvalue")),
                                Optional.empty(),
                                Optional.of(relationPartitions),
                                Collections.emptyList())),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                "hasEvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "Evalue")),
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
                                        Optional.of(entityPartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasRvalue",
                                new GraphElementConstraint.Impl(__.has(T.label, "Rvalue")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "relationId",
                                        Optional.of("Relation"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("relationId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Rvalue"),
                                        valueEdgeRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("relationId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasRelation",
                                new GraphElementConstraint.Impl(__.has(T.label, "Relation.D")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityA.id",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "logicalId",
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasOutRelation",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "Relation.D"), __.has("direction", Direction.OUT.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityA.id",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "logicalId",
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInRelation",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "Relation.D"), __.has("direction", Direction.IN.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityA.id",
                                        Optional.of("Entity"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "logicalId",
                                        Optional.of("Relation"),
                                        relationDualRedundantProperties,
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("logicalId", "string")
                                        )),
                                        Optional.of(entityPartitions))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet())
                        ));
    }
    //endregion
}
