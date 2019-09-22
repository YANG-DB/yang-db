package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Redundant;
import com.yangdb.fuse.model.schema.Relation;
import org.elasticsearch.client.Client;

import java.text.SimpleDateFormat;
import java.util.List;
import javaslang.Tuple2;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory.*;
import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.parseValue;

/**
 * translator that takes the specific ontology with the actual schema and translates the logical graph model into a set of (schematic according to real mapping) elastic documents
 */
public class EntityTransformer implements DataTransformer<DataTransformerContext> {
    public static final String INDEX = "Index";
    public static final String TYPE = "type";
    public static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    private final Ontology.Accessor accessor;
    private IndexProvider indexProvider;
    private final RawSchema schema;
    private final IdGeneratorDriver<Range> idGenerator;
    private final Client client;
    private final ObjectMapper mapper;

    @Inject
    public EntityTransformer(Ontology ontology, IndexProvider indexProvider, RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
        this.accessor = new Ontology.Accessor(ontology);
        this.indexProvider = indexProvider;
        this.schema = schema;
        this.idGenerator = idGenerator;
        this.client = client;
        this.mapper = new ObjectMapper();
    }

    @Override
    public DataTransformerContext transform(LogicalGraphModel graph, GraphDataLoader.Directive directive) {
        DataTransformerContext context = new DataTransformerContext(mapper);
        context.withGraph(graph);
        context.withEntities(graph.getNodes().stream().map(n -> translate(context, n)).collect(Collectors.toList()));
        context.withRelations(graph.getEdges().stream().map(e -> translate(context, e)).collect(Collectors.toList()));
        return context;
    }

    /**
     * translate edge to document
     *
     * @param context
     * @param edge
     * @return
     */
    private DocumentBuilder translate(DataTransformerContext context, LogicalEdge edge) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Relation relation = indexProvider.getRelation(edge.label())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching edge found with label " + edge.label())));
            //put classifiers
            element.put(ID, edge.getId());
            element.put(TYPE, relation.getType());

            //populate metadata
            populateMetadataFields(context, edge, element);
            //populate fields
            populateFields(context, edge, relation, element);

            //partition field in case of none static partitioning index
            Optional<Tuple2<String,String>> partition = Optional.empty();

            //in case of a partition field - set in the document builder
            String field = relation.getProps().getPartitionField();
            if(field !=null)
                partition = Optional.of(new Tuple2<>(field,parseValue(accessor.property$(field).getType(),edge.getProperty(field),sdf).toString()));

            return new DocumentBuilder(element, edge.getId(), relation.getType(), Optional.empty(),partition);
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }


    /**
     * translate vertex to document
     *
     * @param context
     * @param node
     * @return
     */
    private DocumentBuilder translate(DataTransformerContext context, LogicalNode node) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Entity entity = indexProvider.getEntity(node.label())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching node found with label " + node.label())));
            //put classifiers
            element.put(ID, node.getId());
            element.put(TYPE, entity.getType());

            //populate metadata
            populateMetadataFields(context, node, element);

            //populate fields
            populateFields(context, node, entity, element);

            return new DocumentBuilder(element, node.getId(), entity.getType(), Optional.empty());
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }

    /**
     * metadata edge populator
     *
     * @param context
     * @param edge
     * @param element
     */
    private void populateMetadataFields(DataTransformerContext context, LogicalEdge edge, ObjectNode element) {
        edge.metadata().entrySet()
                .stream()
                .filter(m -> accessor.relation$(edge.getLabel()).containsMetadata(m.getKey()))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        parseValue(accessor.property$(m.getKey()).getType(), m.getValue(),sdf).toString()));
    }

    /**
     * metadata vertex populator
     *
     * @param context
     * @param node
     * @param element
     */
    private void populateMetadataFields(DataTransformerContext context, LogicalNode node, ObjectNode element) {
        node.metadata().entrySet()
                .stream()
                .filter(m -> accessor.entity$(node.getLabel()).containsMetadata(m.getKey()))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        parseValue(accessor.property$(m.getKey()).getType(), m.getValue(),sdf).toString()));
    }


    /**
     * fields vertex populator
     *
     * @param context
     * @param node
     * @param entity
     * @param element
     */
    private void populateFields(DataTransformerContext context, LogicalNode node, Entity entity, ObjectNode element) {
        //todo check the structure of the index
        switch (entity.getMapping()) {
            case INDEX:
                //populate properties
                node.fields().entrySet()
                        .stream()
                        .filter(m -> accessor.entity$(node.getLabel()).containsProperty(m.getKey()))
                        .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                                parseValue(accessor.property$(m.getKey()).getType(), m.getValue(),sdf).toString()));
                break;
            // todo manage nested index fields
            default:
        }
    }


    /**
     * populate fields including redundant fields
     *
     * @param edge
     * @param relation
     * @param element
     */
    private ObjectNode populateFields(DataTransformerContext context, LogicalEdge edge, Relation relation, ObjectNode element) {
        //populate redundant fields A
        element.put(ENTITY_A, populateSide(ENTITY_A, context, edge.getSource(), relation));
        //populate redundant fields B
        element.put(ENTITY_B, populateSide(ENTITY_B, context, edge.getTarget(), relation));

        //populate direct fields
        switch (relation.getMapping()) {
            case INDEX:
                //populate properties
                edge.fields().entrySet()
                        .stream()
                        .filter(m -> accessor.relation$(edge.getLabel()).containsProperty(m.getKey()))
                        .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                                parseValue(accessor.property$(m.getKey()).getType(), m.getValue(),sdf).toString()));
                ;
                break;
            // todo manage nested index fields
            default:
        }

        return element;

    }

    /**
     * populate edge redundant side - as a json object
     *
     * @param side
     * @param context
     * @param sideId
     * @param relation
     * @return
     */
    private ObjectNode populateSide(String side, DataTransformerContext context, String sideId, Relation relation) {
        ObjectNode entitySide = mapper.createObjectNode();
        Optional<LogicalNode> source = nodeById(context, sideId);
        if (!source.isPresent()) {
            throw new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching node found with sideId " + sideId));
        }

        //get type (label) of the side node
        Entity entity = indexProvider.getEntity(source.get().label())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching node found with label " + source.get().label())));

        //put classifiers
        entitySide.put(ID, source.get().getId());
        entitySide.put(TYPE, entity.getType());

        List<Redundant> redundant = relation.getRedundant(side);
        redundant.forEach(r -> populateRedundantField(r, source.get(), entitySide));
        return entitySide;
    }

    private void populateRedundantField(Redundant redundant, LogicalNode logicalNode, ObjectNode map) {
        Optional<Object> prop = logicalNode.getPropertyValue(redundant.getRedundantName());
        prop.ifPresent(o -> map.put(redundant.getRedundantName(),
                parseValue(redundant.getType(), o.toString(),sdf).toString()));
    }

    private Optional<LogicalNode> nodeById(DataTransformerContext context, String id) {
        return context.getGraph().getNodes().stream().filter(n -> n.getId().equals(id)).findAny();
    }



}
