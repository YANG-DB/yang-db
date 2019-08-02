package com.yangdb.fuse.assembly.knowledge.load;

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

import com.google.inject.Inject;
import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.Range.StatefulRange;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.ontology.transformer.TransformerEntityType;
import com.yangdb.fuse.model.ontology.transformer.TransformerProperties;
import com.yangdb.fuse.model.ontology.transformer.TransformerRelationType;
import org.elasticsearch.client.Client;
import org.geojson.Point;

import java.text.ParseException;
import java.util.*;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchema.ENTITY;
import static com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder._r;
import static com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder._v;
import static java.util.regex.Pattern.matches;

public class KnowledgeTransformer {
    public static final String TECHNICAL_ID = "techId";
    private static Map<String, StatefulRange> ranges = new HashMap<>();

    private OntologyTransformer transformer;
    private RawSchema schema;
    private IdGeneratorDriver<Range> idGenerator;
    private Client client;
    private KnowledgeWriterContext writerContext;

    @Inject
    public KnowledgeTransformer(OntologyTransformer transformer, RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
        this.transformer = transformer;
        this.schema = schema;
        this.idGenerator = idGenerator;
        this.client = client;
    }

    public KnowledgeContext transform(LogicalGraphModel graph) {
        KnowledgeContext context = new KnowledgeContext();
        this.writerContext = new KnowledgeWriterContext(context);
        //populate context according to given json graph
        for (LogicalNode node : graph.getNodes()) {
            Optional<TransformerEntityType> entityType = transformer.getEntityTypes().stream()
                    .filter(e -> matches(e.getPattern(), node.getLabel())).findFirst();
            if (!entityType.isPresent()) {
                context.failed("Entity type not matched", node.toString());
                continue;
            }

            TransformerEntityType type = entityType.get();
            switch (type.geteType()) {
                case EntityBuilder.type:
                    StatefulRange range = ranges.computeIfAbsent(EntityBuilder.type,
                            s -> new StatefulRange(idGenerator.getNext(EntityBuilder.type, 1000)));
                    context.add(createEntity(context, range, type, node));
                    break;
                case RefBuilder.type:
                    break;
                case FileBuilder.type:
                    break;
                case InsightBuilder.type:
                    break;

            }
        }

        for (LogicalEdge edge : graph.getEdges()) {
            Optional<TransformerRelationType> edgeType = transformer.getRelationTypes().stream()
                    .filter(e -> matches(e.getPattern(), edge.getLabel())).findFirst();
            if (!edgeType.isPresent()) {
                context.failed("Edge type not matched", edge.toString());
                continue;
            }

            TransformerRelationType type = edgeType.get();
            switch (type.getrType()) {
                case RelationBuilder.type:
                    StatefulRange range = ranges.computeIfAbsent(RelationBuilder.type,
                            s -> new StatefulRange(idGenerator.getNext(RelationBuilder.type, 1000)));
                    try {
                        context.add(createEdge(context, range, type, edge));
                    }catch (Throwable err) {
                        //error while creating edge
                    }
                    break;
            }
        }
        return context;
    }

    private RelationBuilder createEdge(KnowledgeContext context, StatefulRange range, TransformerRelationType type, LogicalEdge edge) {
        RelationBuilder builder = _rel(writerContext.nextRelId(schema, range.next()));
        String physicalLabelKey = type.getLabel();
        String labelValue = edge.getLabel();
        builder.putProperty(physicalLabelKey, labelValue);

        //set sides ids
        Optional<EntityBuilder> sideA = writerContext.getContext().findEntityById(edge.getSource()).isPresent()
                ? writerContext.getContext().findEntityById(edge.getSource())
                : writerContext.getContext().findEntityByTechId(edge.getSource());

        if (!sideA.isPresent()) {
            // search Elastic for the given node
            Optional<Map> node = StoreAccessor.findEntityById(TECHNICAL_ID, edge.getSource(), ENTITY, schema, client);

            if (node.isPresent()) {
                builder.entityAId(node.get().get("id").toString());
                builder.entityATechId(node.get().get("techId").toString());
                builder.entityACategory(node.get().get("category").toString());
            } else {
                context.failed(edge.getSource(),String.format("Source node %s for edge not found %s", edge.getSource(), edge.toString()));
                throw new IllegalArgumentException(String.format("Source node %s for edge not found %s", edge.getSource(), edge.toString()));
            }
        } else {
            builder.entityAId(sideA.get().id());
            builder.entityATechId(sideA.get().techId);
            builder.entityACategory(sideA.get().category);
        }


        Optional<EntityBuilder> sideB = writerContext.getContext().findEntityById(edge.getTarget()).isPresent()
                ? writerContext.getContext().findEntityById(edge.getTarget())
                : writerContext.getContext().findEntityByTechId(edge.getTarget());

        if (!sideB.isPresent()) {
            //search Elastic for the given node
            Optional<Map> node = StoreAccessor.findEntityById(TECHNICAL_ID, edge.getTarget(), ENTITY, schema, client);

            if (node.isPresent()) {
                builder.entityBId(node.get().get("id").toString());
                builder.entityBTechId(node.get().get("techId").toString());
                builder.entityBCategory(node.get().get("category").toString());
            } else {
                context.failed(edge.getSource(),String.format("Target node %s for edge not found %s", edge.getTarget(), edge.toString()));
                throw new IllegalArgumentException(String.format("Target node %s for edge not found %s", edge.getSource(), edge.toString()));
            }
        } else {
            builder.entityBId(sideB.get().id());
            builder.entityBTechId(sideB.get().techId);
            builder.entityBCategory(sideB.get().category);
        }

        //populate side with relation builder hasEntityRelation
        if (sideA.isPresent())
            sideA.get().rel(builder, "out");
        else
            context.add(new RelationBuilder.EntityRelationBuilder(builder.entityAId, builder, "out"));

        if (sideB.isPresent())
            sideB.get().rel(builder, "in");
        else
            context.add(new RelationBuilder.EntityRelationBuilder(builder.entityBId, builder, "in"));

        //set metadata properties
        edge.getMetadata().getProperties().forEach((logicalKey, value) -> {
            if (type.hasMetadataProperty(logicalKey)) {
                final Map.Entry<String, String> entry = type.metadataProperty(logicalKey).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                builder.putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                builder.putProperty(logicalKey, value);
            }

        });
        //transform regular properties (RValue)
        StatefulRange propRange = ranges.computeIfAbsent(RvalueBuilder.type,
                s -> new StatefulRange(idGenerator.getNext(RvalueBuilder.type, 1000)));
        edge.getProperties().getProperties().forEach((key, value) -> {
            RvalueBuilder valueBuilder = createRValueBuilder(propRange, type.getProperties(), key, value);
            context.add(valueBuilder);
            builder.value(valueBuilder);

        });

        return builder;
    }

    private EntityBuilder createEntity(KnowledgeContext context, StatefulRange range, TransformerEntityType type, LogicalNode node) {
        //if the "id" field is present -> use it in the techId section
        EntityBuilder builder = _e(writerContext.nextLogicalId(schema, range.next()));
        //technical Id to find the node by (real id is given by the engine sequencer)
        builder.putProperty(TECHNICAL_ID, node.getId());
        String physicalLabelKey = type.getLabel();
        String labelValue = node.getLabel();
        builder.putProperty(physicalLabelKey, labelValue);
        //for each metadata property in the logical graph
        node.getMetadata().getProperties().forEach((logicalKey, value) -> {
            if (type.hasMetadataProperty(logicalKey)) {
                final Map.Entry<String, String> entry = type.metadataProperty(logicalKey).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                builder.putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                builder.putProperty(logicalKey, value);
            }

        });
        //transform regular properties (EValue)
        StatefulRange propRange = ranges.computeIfAbsent(ValueBuilder.type,
                s -> new StatefulRange(idGenerator.getNext(ValueBuilder.type, 1000)));
        node.getProperties().getProperties().forEach((key, value) -> {
            ValueBuilder valueBuilder = createValueBuilder(propRange, type.getProperties(), key, value);
            context.add(valueBuilder);
            builder.value(valueBuilder);
        });

        return builder;
    }

    private ValueBuilder createValueBuilder(StatefulRange propRange, TransformerProperties properties, String key, Object value) {
        ValueBuilder valueBuilder = _v(writerContext.nextValueId(schema, propRange.next()));
        //todo think if TransformerProperties.label pattern is still relevant
        valueBuilder.field(key);
        if(value!=null) {
            //todo think if TransformerProperties.concreteType is still relevant
            String type = properties.getConcreteType();
            //find value explicit type according to pattern
            Optional<Map<String, String>> valueType = properties.getValuePatterns().stream()
                    .filter(Objects::nonNull)
                    .filter(p -> value.toString().matches(p.values().iterator().next()))
                    .findFirst();
            if (valueType.isPresent()) {
                String explicitType = valueType.get().keySet().iterator().next();
                valueBuilder.value(toValue(explicitType, value));
            } else {
                valueBuilder.value(value);
            }
        }
        return valueBuilder;
    }

    private RvalueBuilder createRValueBuilder(StatefulRange propRange, TransformerProperties properties, String key, Object value) {
        RvalueBuilder valueBuilder = _r(writerContext.nextValueId(schema, propRange.next()));
        //todo think if TransformerProperties.label pattern is still relevant
        valueBuilder.field(key);
        //todo think if TransformerProperties.concreteType is still relevant
        String type = properties.getConcreteType();
        //find value explicit type according to pattern
        Optional<Map<String, String>> valueType = properties.getValuePatterns().stream()
                .filter(p -> value.toString().matches(p.values().iterator().next()))
                .findFirst();
        if (valueType.isPresent()) {
            String explicitType = valueType.get().keySet().iterator().next();
            valueBuilder.value(toValue(explicitType, value));
        } else {
            valueBuilder.value(value);
        }
        return valueBuilder;
    }

    private Object toValue(String explicitType, Object value) {
        switch (explicitType) {
            case "stringValue":
                return value.toString();
            case "intValue":
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    try {
                        return Long.valueOf(value.toString());
                    } catch (NumberFormatException e1) {
                        return value.toString();
                    }
                }
            case "longValue":
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    return value.toString();
                }
            case "floatValue":
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    return value.toString();
                }
            case "dateValue":
                try {
                    return Metadata.sdf.parse(value.toString());
                } catch (ParseException e) {
                    try {
                        return new Date(value.toString());
                    }catch (Throwable e1) {
                        return value.toString();
                    }
                }
            case "geoValue":
                return new Point(
                        Double.valueOf(value.toString().split("[,]")[0]),
                        Double.valueOf(value.toString().split("[,]")[1]));
        }
        return value.toString();
    }
}
