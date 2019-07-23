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
import org.geojson.Point;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext.format;
import static com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder.*;
import static com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder.*;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder.*;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder.*;
import static java.util.regex.Pattern.matches;

public class KnowledgeTransformer {
    public static final String TECHNICAL_ID = "techId";

    private OntologyTransformer transformer;
    private RawSchema schema;
    private IdGeneratorDriver<Range> idGenerator;
    private Map<String, StatefulRange> ranges;
    private KnowledgeWriterContext writerContext;

    public KnowledgeTransformer(OntologyTransformer transformer, RawSchema schema, IdGeneratorDriver<Range> idGenerator) {
        this.transformer = transformer;
        this.schema = schema;
        this.idGenerator = idGenerator;
        this.ranges = new HashMap<>();

    }

    public KnowledgeContext transform(LogicalGraphModel graph) {
        KnowledgeContext context = new KnowledgeContext();
        this.writerContext = new KnowledgeWriterContext(context);
        //populate context according to given json graph
        graph.getNodes().forEach(node -> {
            Optional<TransformerEntityType> entityType = transformer.getEntityTypes().stream()
                    .filter(e -> matches(e.getPattern(), node.getLabel())).findFirst();
            if (!entityType.isPresent())
                context.failed("Entity type not matched", node.toString());

            TransformerEntityType type = entityType.get();
            switch (type.geteType()) {
                case EntityBuilder.type:
                    StatefulRange range = ranges.computeIfAbsent(EntityBuilder.type,
                            s -> new StatefulRange(idGenerator.getNext(EntityBuilder.type, 1000)));
                    context.add(createEntity(context,range, type, node));
                    break;
                case RefBuilder.type:
                    break;
                case FileBuilder.type:
                    break;
                case InsightBuilder.type:
                    break;

            }
        });

        graph.getEdges().forEach(edge -> {
            Optional<TransformerRelationType> edgeType = transformer.getRelationTypes().stream()
                    .filter(e -> matches(e.getPattern(), edge.getLabel())).findFirst();
            if (!edgeType.isPresent())
                context.failed("Edge type not matched", edge.toString());

            TransformerRelationType type = edgeType.get();
            switch (type.getrType()) {
                case RelationBuilder.type:
                    StatefulRange range = ranges.computeIfAbsent(RelationBuilder.type,
                            s -> new StatefulRange(idGenerator.getNext(RelationBuilder.type, 1000)));
                    context.add(createEdge(context,range, type, edge));
                    break;
            }
        });
        return context;
    }

    private RelationBuilder createEdge(KnowledgeContext context, StatefulRange range, TransformerRelationType type, LogicalEdge edge) {
        RelationBuilder builder = _rel(writerContext.nextRelId(schema,range.next()));
        String physicalLabelKey = type.getLabel();
        String labelValue = edge.getLabel();
        builder.putProperty(physicalLabelKey, labelValue);
        //set sides ids
        Optional<EntityBuilder> sideA = writerContext.getContext().findEntityByProperty(TECHNICAL_ID, edge.getSource());
        if(!sideA.isPresent())
            //todo search Elastic for the given node
            throw new IllegalArgumentException(String.format("Source node %s for edge not found %s",edge.getSource(),edge.toString()));
        builder.entityAId(sideA.get().id());
        builder.entityACategory(sideA.get().category);

        Optional<EntityBuilder> sideB = writerContext.getContext().findEntityByProperty(TECHNICAL_ID, edge.getTarget());
        if(!sideB.isPresent())
            //todo search Elastic for the given node
            throw new IllegalArgumentException(String.format("Source node %s for edge not found %s",edge.getTarget(),edge.toString()));
        builder.entityBId(sideB.get().id());
        builder.entityBCategory(sideB.get().category);

        //populate side with relation builder hasEntityRelation
        sideA.get().rel(builder,"out");
        sideB.get().rel(builder,"in");

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

    private EntityBuilder createEntity(KnowledgeContext context,StatefulRange range, TransformerEntityType type, LogicalNode node) {
        //todo what if the "id" field is present -> should we use it instead of the auto id generator ??
        EntityBuilder builder = _e(writerContext.nextLogicalId(schema, range.next()));
        //technical Id to find the node by (real id is given by the engine sequencer)
        builder.putProperty(TECHNICAL_ID,node.getId());
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
                return Integer.valueOf(value.toString());
            case "longValue":
                return Long.valueOf(value.toString());
            case "floatValue":
                return Float.valueOf(value.toString());
            case "dateValue":
                try {
                    return Metadata.sdf.parse(value.toString());
                } catch (ParseException e) {
                    return new Date(value.toString());
                }
            case "geoValue":
                return new Point(
                        Double.valueOf(value.toString().split("[,]")[0]),
                        Double.valueOf(value.toString().split("[,]")[1]));
        }
        return value.toString();
    }
}
