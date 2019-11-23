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

import com.google.inject.Inject;
import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.Range.StatefulRange;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.ontology.transformer.TransformerEntityType;
import com.yangdb.fuse.model.ontology.transformer.TransformerRelationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeLoaderUtils.*;
import static java.util.regex.Pattern.matches;

public class KnowledgeGraphTransformer implements DataTransformer<KnowledgeContext,LogicalGraphModel> {
    private static Map<String, StatefulRange> ranges = new HashMap<>();

    private OntologyTransformer transformer;
    private RawSchema schema;
    private IdGeneratorDriver<Range> idGenerator;
    private StoreAccessor accessor;
    private KnowledgeWriterContext writerContext;

    @Inject
    public KnowledgeGraphTransformer(RawSchema schema, OntologyTransformer transformer, IdGeneratorDriver<Range> idGenerator, StoreAccessor client) {
        this.transformer = transformer;
        this.schema = schema;
        this.idGenerator = idGenerator;
        this.accessor = client;
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
                    try {
                        context.add(createEntity(accessor,schema,
                                context,writerContext,
                                getRange(ranges,idGenerator,EntityBuilder.type),
                                getRange(ranges,idGenerator,ValueBuilder.type),
                                type,
                                node.getId(),
                                node.getLabel(),
                                node.getMetadata().getProperties(),
                                node.getProperties().getProperties(),
                                directive));
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
                    try {
                        //id generation for edges without ids: source_label_target
                        String id = (edge.getId() != null) ? edge.getId() : String.format("%s_%s_%s",edge.source(),edge.label(),edge.target()) ;

                        context.add(createEdge(accessor,schema,
                                context,writerContext,
                                getRange(ranges,idGenerator,RelationBuilder.type),
                                getRange(ranges,idGenerator,RvalueBuilder.type),
                                type,
                                id,
                                edge.getLabel(),
                                edge.source(),
                                edge.target(),
                                edge.getMetadata().getProperties(),
                                edge.getProperties().getProperties(),
                                directive));
                    } catch (Throwable err) {
                        //error while creating edge
                        context.failed("Edge creation failed", err.getMessage());
                    }
                    break;
            }
        }
        return context;
    }



}
