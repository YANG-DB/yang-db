package com.kayhut.fuse.assembly.knowledge.load;

import com.kayhut.fuse.assembly.knowledge.load.builder.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.FileBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.InsightBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.RefBuilder;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.Range.StatefulRange;
import com.kayhut.fuse.model.logical.LogicalGraphModel;
import com.kayhut.fuse.model.logical.LogicalNode;
import com.kayhut.fuse.model.ontology.transformer.OntologyTransformer;
import com.kayhut.fuse.model.ontology.transformer.TransformerEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.kayhut.fuse.assembly.knowledge.load.builder.EntityBuilder._e;
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
                    .filter(e -> matches(e.getPattern(), node.getMetadata().getLabel())).findFirst();
            if(!entityType.isPresent())
                context.failed("Entity type not matched",node.toString());

            TransformerEntityType type = entityType.get();
            switch (type.geteType()) {
                case EntityBuilder.type:
                    StatefulRange range = ranges.computeIfAbsent(EntityBuilder.type,
                            s -> new StatefulRange(idGenerator.getNext(EntityBuilder.type, 1000)));
                    context.add(createEntity(range,type,node));
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
        EntityBuilder builder = _e(writerContext.nextLogicalId(schema,range.next()));
        //for each metadata property in the logical graph
        node.getMetadata().getProperties().forEach((logicalKey,value)-> {
            if(type.hasMetadataProperty(logicalKey)) {
                final Map.Entry<String, String> entry = type.metadataProperty(logicalKey).get().entrySet().iterator().next();
                final String physicalKey = entry.getValue();
                //set physical metadata properties
                builder.putProperty(physicalKey, value);
            } else {
                //set logical metadata properties
                builder.putProperty(logicalKey, value);
            }

        });
        //todo transform regular properties (EValue)


        return builder;
    }
}
