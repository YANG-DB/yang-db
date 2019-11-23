package com.yangdb.fuse.assembly.knowledge.load;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.transformer.TransformerEntityType;
import com.yangdb.fuse.model.ontology.transformer.TransformerProperties;
import com.yangdb.fuse.model.ontology.transformer.TransformerRelationType;
import org.geojson.Point;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.InflaterInputStream;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort.ENTITY;
import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort.RELATION;
import static com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder._r;
import static com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder._v;

/**
 * utility loader helper class consisting of general purpose methods used by Graph & CSV loaders
 */
public abstract class KnowledgeLoaderUtils {
    public static final int BULK_SIZE = 10;

    public static final String ID = "id";
    public static final String CATEGORY = "category";
    public static final String TECH_ID = "techId";

    /**
     * get next id sequence range from DB
     * @param ranges
     * @param idGenerator
     * @param type
     * @return
     */
    public static Range.StatefulRange getRange(Map<String, Range.StatefulRange> ranges, IdGeneratorDriver<Range> idGenerator, String type) {
        //init ranges
        Range.StatefulRange statefulRange = ranges.computeIfAbsent(type,
                s -> new Range.StatefulRange(idGenerator.getNext(type, BULK_SIZE)));

        if (statefulRange.hasNext())
            return statefulRange;
        //update ranges
        ranges.put(type, new Range.StatefulRange(idGenerator.getNext(type, BULK_SIZE)));
        //return next range
        return ranges.get(type);
    }

    /**
     * translate string value to object value (unBox)
     * @param explicitType
     * @param value
     * @return
     */
    public static Object toValue(String explicitType, Object value) {
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

    /**
     * create Knowledge Entity document builder
     * @param client
     * @param schema
     * @param context
     * @param writerContext
     * @param entityRange
     * @param entityValueRange
     * @param type
     * @param id
     * @param label
     * @param metadata
     * @param properties
     * @param directive
     * @return
     */
    public static EntityBuilder createEntity(StoreAccessor client, RawSchema schema,
                                             //context info
                                             KnowledgeContext context, KnowledgeWriterContext writerContext,
                                             //db sequence range
                                             Range.StatefulRange entityRange, Range.StatefulRange entityValueRange,
                                             //ontology info
                                             TransformerEntityType type,
                                             //node info
                                             String id, String label, Map<String, ?> metadata, Map<String, ?> properties,
                                             //upsert directive
                                             GraphDataLoader.Directive directive) {
        AtomicReference<EntityBuilder> builder = new AtomicReference<>();
        //if the "id" field is present -> use it in the techId section
        if (directive == GraphDataLoader.Directive.INSERT) {
            builder.set(_e(writerContext.nextLogicalId(schema, entityRange.next())));
        } else {
            //check by techId for existence of edge entity in DB
            Optional<Map> source = client.findEntityById(TECH_ID, id, ENTITY, schema);
            //the insert part of the upsert
            if(!source.isPresent()) {
                builder.set(_e(writerContext.nextLogicalId(schema, entityRange.next())));
            } else {
                builder.set(_e(source.get().get(ID).toString().split("[.]")[0]));
            }
        }
        //technical Id to find the node by (real id is given by the engine sequencer)
        builder.get().putProperty(TECH_ID, id);
        String physicalLabelKey = type.getLabel();
        String labelValue = label;
        builder.get().putProperty(physicalLabelKey, labelValue);
        //for each metadata property in the logical graph
        metadata.forEach((logicalKey, value) -> {
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
        properties.forEach((key, value) -> {
            ValueBuilder valueBuilder = createValueBuilder(schema,writerContext,entityValueRange, type.getProperties(), key, value);
            context.add(valueBuilder);
            builder.get().value(valueBuilder);
        });

        return builder.get();
    }

    /**
     * create Knowledge EntityValue document builder
     * @param schema
     * @param writerContext
     * @param propRange
     * @param properties
     * @param key
     * @param value
     * @return
     */
    public static ValueBuilder createValueBuilder(RawSchema schema, KnowledgeWriterContext writerContext, Range.StatefulRange propRange, TransformerProperties properties, String key, Object value) {
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

    /**
     * create Knowledge Relation document builder
     * @param client
     * @param schema
     * @param context
     * @param writerContext
     * @param entityRange
     * @param valueRange
     * @param type
     * @param id
     * @param label
     * @param source
     * @param target
     * @param metadata
     * @param properties
     * @param directive
     * @return
     */
    public static RelationBuilder createEdge(StoreAccessor client, RawSchema schema,
                                             //context info
                                             KnowledgeContext context, KnowledgeWriterContext writerContext,
                                             //db sequence range
                                             Range.StatefulRange entityRange, Range.StatefulRange valueRange,
                                             //ontology info
                                             TransformerRelationType type,
                                             //node info
                                             String id, String label,String source,String target,
                                             //node properties
                                             Map<String, ?> metadata, Map<String, ?> properties,
                                             //upsert directive
                                             GraphDataLoader.Directive directive) {
        AtomicReference<RelationBuilder> builder = new AtomicReference<>();
        if (directive == GraphDataLoader.Directive.INSERT) {
            builder.set(_rel(writerContext.nextRelId(schema, entityRange.next())));
        } else {
            //check by techId for existence of edge entity in DB
            Optional<Map> node = client.findEntityById(TECH_ID, id, RELATION, schema);
            //the insert part of the upsert
            if(!node.isPresent()) {
                builder.set(_rel(writerContext.nextRelId(schema, entityRange.next())));
            } else {
                builder.set(_rel(node.get().get(ID).toString().split("[.]")[0]));
            }
        }


        String physicalLabelKey = type.getLabel();
        String labelValue = label;
        builder.get().putProperty(physicalLabelKey, labelValue);
        //technical Id to find the node by (real id is given by the engine sequencer)
        builder.get().putProperty(TECH_ID, id);

        //set sides ids
        Optional<EntityBuilder> sideA = writerContext.getContext().findEntityById(source).isPresent()
                ? writerContext.getContext().findEntityById(source)
                : writerContext.getContext().findEntityByTechId(source);

        if (!sideA.isPresent()) {
            // search Elastic for the given node
            Optional<Map> node = client.findEntityById(TECH_ID, source, ENTITY, schema);

            if (node.isPresent()) {
                builder.get().entityAId(node.get().get(ID).toString());
                builder.get().entityATechId(node.get().get(TECH_ID).toString());
                builder.get().entityACategory(node.get().get(CATEGORY).toString());
            } else {
                context.failed(source, String.format("Source node %s for edge not found %s", source, id));
                throw new IllegalArgumentException(String.format("Source node %s for edge not found %s", source, id));
            }
        } else {
            builder.get().entityAId(sideA.get().id());
            builder.get().entityATechId(sideA.get().techId);
            builder.get().entityACategory(sideA.get().category);
        }


        Optional<EntityBuilder> sideB = writerContext.getContext().findEntityById(target).isPresent()
                ? writerContext.getContext().findEntityById(target)
                : writerContext.getContext().findEntityByTechId(target);

        if (!sideB.isPresent()) {
            //search Elastic for the given node
            Optional<Map> node = client.findEntityById(TECH_ID, target, ENTITY, schema);

            if (node.isPresent()) {
                builder.get().entityBId(node.get().get(ID).toString());
                builder.get().entityBTechId(node.get().get(TECH_ID).toString());
                builder.get().entityBCategory(node.get().get(CATEGORY).toString());
            } else {
                context.failed(source, String.format("Target node %s for edge not found %s", target, id));
                throw new IllegalArgumentException(String.format("Target node %s for edge not found %s", source, id));
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
        metadata.forEach((key, value) -> {
            if (type.hasMetadataProperty(key)) {
                final Map.Entry<String, String> entry = type.metadataProperty(key).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                builder.get().putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                builder.get().putProperty(key, value);
            }

        });
        //transform regular properties (RValue)
        properties.forEach((key, value) -> {
            if(!type.hasMetadataProperty(key)) {
                RvalueBuilder valueBuilder = createRValueBuilder(schema, writerContext, valueRange, type.getProperties(), key, value);
                context.add(valueBuilder);
                builder.get().value(valueBuilder);
            }
        });

        return builder.get();
    }

    /**
     * create Knowledge RelValue document builder
     *
     * @param schema
     * @param writerContext
     * @param propRange
     * @param properties
     * @param key
     * @param value
     * @return
     */
    public static RvalueBuilder createRValueBuilder(RawSchema schema, KnowledgeWriterContext writerContext,Range.StatefulRange propRange, TransformerProperties properties, String key, Object value) {
        RvalueBuilder valueBuilder = _r(writerContext.nextRvalueId(schema, propRange.next()));
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

}
