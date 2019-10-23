package com.yangdb.fuse.assembly.knowledge.load;

/*-
 *
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
 *
 */

import com.google.inject.Inject;
import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort.*;
import static com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.load.builder.Metadata.sdf;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder._r;
import static com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder._v;
import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.parseValue;
import static java.util.regex.Pattern.matches;

public class KnowledgeTransformer implements DataTransformer<KnowledgeContext> {
    public static final int BULK_SIZE = 10;
    public static final String TECH_ID = "techId";
    public static final String ID = "id";
    public static final String CATEGORY = "category";
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

    @Override
    public KnowledgeContext transform(LogicalGraphModel graph, GraphDataLoader.Directive directive) {
        KnowledgeContext context = new KnowledgeContext();
        this.writerContext = new KnowledgeWriterContext(context);
        //populate context according to given json graph
        for (LogicalNode node : graph.getNodes()) {
            Optional<TransformerEntityType> entityType = transformer.getEntityTypes().stream()
                    .filter(e -> matches(e.getPattern(), node.label())).findFirst();
            if (!entityType.isPresent()) {
                context.failed("Entity type not matched", node.toString());
                continue;
            }

            TransformerEntityType type = entityType.get();
            switch (type.geteType()) {
                case EntityBuilder.type:
                    StatefulRange range = getRange(EntityBuilder.type);
                    try {
                        context.add(createEntity(context, range, type, node, directive));
                    }catch (Throwable err) {
                        //error while creating edge
                        context.failed("Vertex creation failed", err.getMessage());
                    }
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
                    StatefulRange range = getRange(RelationBuilder.type);
                    try {
                        context.add(createEdge(context, range, type, edge, directive));
                    } catch (Throwable err) {
                        //error while creating edge
                        context.failed("Edge creation failed", err.getMessage());
                    }
                    break;
            }
        }
        return context;
    }

    public StatefulRange getRange(String type) {
        //init ranges
        StatefulRange statefulRange = ranges.computeIfAbsent(type,
                s -> new StatefulRange(idGenerator.getNext(type, BULK_SIZE)));

        if (statefulRange.hasNext())
            return statefulRange;
        //update ranges
        ranges.put(type, new StatefulRange(idGenerator.getNext(type, BULK_SIZE)));
        //return next range
        return ranges.get(type);
    }

    private RelationBuilder createEdge(KnowledgeContext context, StatefulRange range, TransformerRelationType type, LogicalEdge edge, GraphDataLoader.Directive directive) {
        AtomicReference<RelationBuilder> builder = new AtomicReference<>();
        if (directive == GraphDataLoader.Directive.INSERT) {
            builder.set(_rel(writerContext.nextRelId(schema, range.next())));
        } else {
            //check by techId for existence of edge entity in DB
            Optional<Map> node = StoreAccessor.findEntityById(TECH_ID, edge.id(), RELATION, schema, client);
            //the insert part of the upsert
            if(!node.isPresent()) {
                builder.set(_rel(writerContext.nextRelId(schema, range.next())));
            } else {
                builder.set(_rel(node.get().get(ID).toString().split("[.]")[0]));
            }
        }


        String physicalLabelKey = type.getLabel();
        String labelValue = edge.getLabel();
        builder.get().putProperty(physicalLabelKey, labelValue);

        //set sides ids
        Optional<EntityBuilder> sideA = writerContext.getContext().findEntityById(edge.getSource()).isPresent()
                ? writerContext.getContext().findEntityById(edge.getSource())
                : writerContext.getContext().findEntityByTechId(edge.getSource());

        if (!sideA.isPresent()) {
            // search Elastic for the given node
            Optional<Map> node = StoreAccessor.findEntityById(TECH_ID, edge.getSource(), ENTITY, schema, client);

            if (node.isPresent()) {
                builder.get().entityAId(node.get().get(ID).toString());
                builder.get().entityATechId(node.get().get(TECH_ID).toString());
                builder.get().entityACategory(node.get().get(CATEGORY).toString());
            } else {
                context.failed(edge.getSource(), String.format("Source node %s for edge not found %s", edge.getSource(), edge.toString()));
                throw new IllegalArgumentException(String.format("Source node %s for edge not found %s", edge.getSource(), edge.toString()));
            }
        } else {
            builder.get().entityAId(sideA.get().id());
            builder.get().entityATechId(sideA.get().techId);
            builder.get().entityACategory(sideA.get().category);
        }


        Optional<EntityBuilder> sideB = writerContext.getContext().findEntityById(edge.getTarget()).isPresent()
                ? writerContext.getContext().findEntityById(edge.getTarget())
                : writerContext.getContext().findEntityByTechId(edge.getTarget());

        if (!sideB.isPresent()) {
            //search Elastic for the given node
            Optional<Map> node = StoreAccessor.findEntityById(TECH_ID, edge.getTarget(), ENTITY, schema, client);

            if (node.isPresent()) {
                builder.get().entityBId(node.get().get(ID).toString());
                builder.get().entityBTechId(node.get().get(TECH_ID).toString());
                builder.get().entityBCategory(node.get().get(CATEGORY).toString());
            } else {
                context.failed(edge.getSource(), String.format("Target node %s for edge not found %s", edge.getTarget(), edge.toString()));
                throw new IllegalArgumentException(String.format("Target node %s for edge not found %s", edge.getSource(), edge.toString()));
            }
        } else {
            builder.get().entityBId(sideB.get().id());
            builder.get().entityBTechId(sideB.get().techId);
            builder.get().entityBCategory(sideB.get().category);
        }

        //populate side with relation builder hasEntityRelation
        if (sideA.isPresent())
            sideA.get().rel(builder.get(), "out");
        else
            context.add(new RelationBuilder.EntityRelationBuilder(builder.get().entityAId, builder.get(), "out"));

        if (sideB.isPresent())
            sideB.get().rel(builder.get(), "in");
        else
            context.add(new RelationBuilder.EntityRelationBuilder(builder.get().entityBId, builder.get(), "in"));

        //set metadata properties
        edge.getMetadata().getProperties().forEach((logicalKey, value) -> {
            if (type.hasMetadataProperty(logicalKey)) {
                final Map.Entry<String, String> entry = type.metadataProperty(logicalKey).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                builder.get().putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                builder.get().putProperty(logicalKey, value);
            }

        });
        //transform regular properties (RValue)
        StatefulRange propRange = getRange(RvalueBuilder.type);
        edge.getProperties().getProperties().forEach((key, value) -> {
            RvalueBuilder valueBuilder = createRValueBuilder(propRange, type.getProperties(), key, value);
            context.add(valueBuilder);
            builder.get().value(valueBuilder);

        });

        return builder.get();
    }

    private EntityBuilder createEntity(KnowledgeContext context, StatefulRange range, TransformerEntityType type, LogicalNode node, GraphDataLoader.Directive directive) {
        AtomicReference<EntityBuilder> builder = new AtomicReference<>();
        //if the "id" field is present -> use it in the techId section
        if (directive == GraphDataLoader.Directive.INSERT) {
            builder.set(_e(writerContext.nextLogicalId(schema, range.next())));
        } else {
            //check by techId for existence of edge entity in DB
            Optional<Map> source = StoreAccessor.findEntityById(TECH_ID, node.getId(), ENTITY, schema, client);
            //the insert part of the upsert
            if(!source.isPresent()) {
                builder.set(_e(writerContext.nextLogicalId(schema, range.next())));
            } else {
                builder.set(_e(source.get().get(ID).toString().split("[.]")[0]));
            }
        }
        //technical Id to find the node by (real id is given by the engine sequencer)
        builder.get().putProperty(TECH_ID, node.getId());
        String physicalLabelKey = type.getLabel();
        String labelValue = node.label();
        builder.get().putProperty(physicalLabelKey, labelValue);
        //for each metadata property in the logical graph
        node.metadata().forEach((logicalKey, value) -> {
            if (type.hasMetadataProperty(logicalKey)) {
                final Map.Entry<String, String> entry = type.metadataProperty(logicalKey).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                builder.get().putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                builder.get().putProperty(logicalKey, value);
            }

        });
        //transform regular properties (EValue)
        StatefulRange propRange = getRange(ValueBuilder.type);
        node.fields().forEach((key, value) -> {
            ValueBuilder valueBuilder = createValueBuilder(propRange, type.getProperties(), key, value);
            context.add(valueBuilder);
            builder.get().value(valueBuilder);
        });

        return builder.get();
    }

    private ValueBuilder createValueBuilder(StatefulRange propRange, TransformerProperties properties, String key, Object value) {
        ValueBuilder valueBuilder = _v(writerContext.nextValueId(schema, propRange.next()));
        //todo think if TransformerProperties.label pattern is still relevant
        valueBuilder.field(key);
        if (value != null) {
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
                    } catch (Throwable e1) {
                        return value.toString();
                    }
                }
            case "geoValue":
                return new Point(
                        Double.valueOf(value.toString().split("[,]")[1]),
                        Double.valueOf(value.toString().split("[,]")[0]));
        }
        return value.toString();
    }
}
