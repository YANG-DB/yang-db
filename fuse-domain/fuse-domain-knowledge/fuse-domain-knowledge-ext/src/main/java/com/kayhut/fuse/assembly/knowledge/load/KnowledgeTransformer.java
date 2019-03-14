package com.kayhut.fuse.assembly.knowledge.load;

import com.kayhut.fuse.assembly.knowledge.load.builder.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.FileBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.InsightBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.RefBuilder;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.Range.StatefulRange;
import com.kayhut.fuse.model.logical.LogicalGraphModel;
import com.kayhut.fuse.model.logical.LogicalNode;
import com.kayhut.fuse.model.ontology.transformer.OntologyTransformer;
import com.kayhut.fuse.model.ontology.transformer.TransformerEntityType;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.matches;

public class KnowledgeTransformer {
    private OntologyTransformer transformer;
    private IdGeneratorDriver<Range> idGenerator;
    private Map<String, StatefulRange> ranges;
    private KnowledgeWriterContext writerContext;

    public KnowledgeTransformer(OntologyTransformer transformer, IdGeneratorDriver<Range> idGenerator) {
        this.transformer = transformer;
        this.idGenerator = idGenerator;

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
        EntityBuilder builder = EntityBuilder._e(writerContext.nextLogicalId(range.next()));
        //todo transform metadata properties
        //todo transform regular properties (EValue)


        return builder;
    }
}
