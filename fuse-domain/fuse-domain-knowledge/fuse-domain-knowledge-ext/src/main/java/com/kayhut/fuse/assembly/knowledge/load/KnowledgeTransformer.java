package com.kayhut.fuse.assembly.knowledge.load;

import com.kayhut.fuse.assembly.knowledge.load.builder.*;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.Range.StatefulRange;
import com.kayhut.fuse.model.logical.LogicalGraphModel;
import com.kayhut.fuse.model.logical.LogicalNode;
import com.kayhut.fuse.model.ontology.transformer.OntologyTransformer;
import com.kayhut.fuse.model.ontology.transformer.TransformerEntityType;
import com.kayhut.fuse.model.ontology.transformer.TransformerProperties;
import org.geojson.Point;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.assembly.knowledge.load.builder.EntityBuilder.*;
import static com.kayhut.fuse.assembly.knowledge.load.builder.ValueBuilder._v;
import static java.util.regex.Pattern.matches;

public class KnowledgeTransformer {
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
                    context.add(createEntity(range, type, node));
                    break;
                case RefBuilder.type:
                    break;
                case FileBuilder.type:
                    break;
                case InsightBuilder.type:
                    break;

            }
        });
        graph.getEdges();
        return context;
    }

    private EntityBuilder createEntity(StatefulRange range, TransformerEntityType type, LogicalNode node) {
        //todo what if the "id" field is present -> should we use it instead of the auto id generator ??
        EntityBuilder entityBuilder = _e(writerContext.nextLogicalId(schema, range.next()));
        String physicalLabelKey = type.getLabel();
        String labelValue = node.getLabel();
        entityBuilder.putProperty(physicalLabelKey, labelValue);
        //for each metadata property in the logical graph
        node.getMetadata().getProperties().forEach((logicalKey, value) -> {
            if (type.hasMetadataProperty(logicalKey)) {
                final Map.Entry<String, String> entry = type.metadataProperty(logicalKey).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                entityBuilder.putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                entityBuilder.putProperty(logicalKey, value);
            }

        });
        //transform regular properties (EValue)
        StatefulRange propRange = ranges.computeIfAbsent(ValueBuilder.type,
                s -> new StatefulRange(idGenerator.getNext(ValueBuilder.type, 1000)));
        node.getProperties().getProperties().forEach((key, value) -> {
            entityBuilder.value(createValueBuilder(propRange, type.getProperties(), key, value));
        });

        return entityBuilder;
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

    private Object toValue(String explicitType, Object value) {
        switch (explicitType) {
            case "stringValue":
                return value.toString();
            case "intValue":
                return Integer.valueOf(value.toString());
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
