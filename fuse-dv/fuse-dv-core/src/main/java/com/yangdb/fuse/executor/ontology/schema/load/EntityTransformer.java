package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Redundant;
import com.yangdb.fuse.model.schema.Relation;
import javaslang.Tuple2;
import org.opensearch.client.Client;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory.*;
import static com.yangdb.fuse.executor.ontology.DataTransformer.Utils.INDEX;
import static com.yangdb.fuse.executor.ontology.DataTransformer.Utils.sdf;
import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.parseValue;

/**
 * translator that takes the specific ontology with the actual schema and translates the logical graph model into a set of (schematic according to real mapping) elastic documents
 */
public class EntityTransformer implements DataTransformer<DataTransformerContext<LogicalGraphModel>, LogicalGraphModel> {


    private final Ontology.Accessor accessor;
    private IndexProvider indexProvider;
    private final RawSchema schema;
    private final IdGeneratorDriver<Range> idGenerator;
    private final Client client;
    private final ObjectMapper mapper;

    @Inject
    public EntityTransformer(Config config, OntologyProvider ontology, IndexProviderFactory indexProvider, RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
        String assembly = config.getString("assembly");
        this.accessor = new Ontology.Accessor(ontology.get(assembly).orElseThrow(
                () -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));
        //if no index provider found with assembly name - generate default one accoring to ontology and simple Static Index Partitioning strategy
        this.indexProvider = indexProvider.get(assembly).orElseGet(() -> IndexProvider.Builder.generate(accessor.get()));
        this.schema = schema;
        this.idGenerator = idGenerator;
        this.client = client;
        this.mapper = new ObjectMapper();
    }

    @Override
    public DataTransformerContext<LogicalGraphModel> transform(LogicalGraphModel graph, GraphDataLoader.Directive directive) {
        DataTransformerContext<LogicalGraphModel> context = new DataTransformerContext<>(mapper);
        context.withContainer(graph);
        context.withEntities(graph.getNodes().stream().map(n -> translate(context, n)).collect(Collectors.toList()));
        //out direction
        context.withRelations(graph.getEdges().stream().map(e -> translate(context, e, "out")).collect(Collectors.toList()));
        //in direction
        context.withRelations(graph.getEdges().stream().map(e -> translate(context, e, "in")).collect(Collectors.toList()));
        return context;
    }

    /**
     * translate edge to document
     *
     * @param context
     * @param edge
     * @param direction
     * @return
     */
    private DocumentBuilder translate(DataTransformerContext<LogicalGraphModel> context, LogicalEdge edge, String direction) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Relation relation = indexProvider.getRelation(edge.label())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching edge found with label " + edge.label())));
            //put classifiers
            String id = String.format("%s.%s", edge.getId(), direction);
            element.put(ID, id);
            element.put(TYPE, relation.getType());
            element.put(DIRECTION, direction);

            //populate metadata
            populateMetadataFields(mapper, indexProvider, accessor, context, edge, element);
            //populate fields
            populateFields(context, edge, relation, direction, element);

            //partition field in case of none static partitioning index
            Optional<Tuple2<String, String>> partition = Optional.empty();

            //in case of a partition field - set in the document builder
            String field = relation.getProps().getPartitionField();
            if (field != null)
                partition = Optional.of(new Tuple2<>(field, parseValue(accessor.property$(field).getType(), edge.getProperty(field), sdf).toString()));

            return new DocumentBuilder(element, id, relation.getType(), Optional.empty(), partition);
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
    private DocumentBuilder translate(DataTransformerContext<LogicalGraphModel> context, LogicalNode node) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Entity entity = indexProvider.getEntity(node.label())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching node found with label " + node.label())));
            //translate entity
            translateEntity(mapper, indexProvider, accessor, context, node, element, entity);

            return new DocumentBuilder(element, node.getId(), entity.getType(), Optional.empty());
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }

    static ObjectNode populate(ObjectMapper mapper, Ontology.Accessor accessor, IndexProvider indexProvider, DataTransformerContext<LogicalGraphModel> context, ObjectNode element, Map.Entry<String, Object> m) {
        String pType = accessor.property$(m.getKey()).getpType();
        String type = accessor.property$(m.getKey()).getType();

        Object result = parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf);

        //case of primitive type
        if (String.class.isAssignableFrom(result.getClass())) {
            return element.put(pType, result.toString());
        }

        //check property is of type struct  -
        if (accessor.entity(type).isPresent() && indexProvider.getEntity(type).isPresent()) {
            //if struct manage as entity
            if (Collection.class.isAssignableFrom(result.getClass())) {
                AtomicInteger index = new AtomicInteger();
                ArrayNode nodes = element.putArray(pType);
                ((Collection) result).forEach(e -> {
                    try {
                        ObjectNode nested = mapper.createObjectNode();
                        LogicalNode node = mapper.readValue(mapper.writeValueAsString(e), LogicalNode.class);
                        //set metadata
                        node.setId(String.format("%s.%d", pType, index.incrementAndGet()));
                        node.setLabel(type);
                        //transform into document
                        translateEntity(mapper, indexProvider, accessor, context, node, nested, indexProvider.getEntity(type).get());
                        nodes.add(nested);
                    } catch (IOException ex) {
                        nodes.addPOJO(e);
                    }
                });
                return element;
            } else {
                return element.putPOJO(pType, result);
            }
        }
        //all primitive non string types
        return element.put(pType, result.toString());
    }

    static void translateEntity(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalNode node, ObjectNode element, Entity entity) {
        element.put(ID, node.getId());
        element.put(TYPE, entity.getType());

        //populate metadata
        populateMetadataFields(mapper, indexProvider, accessor, context, node, element);

        //populate fields
        populateFields(mapper, indexProvider, accessor, context, node, entity, element);
    }

    /**
     * metadata edge populator
     *
     * @param context
     * @param edge
     * @param element
     */
    public static void populateMetadataFields(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalEdge edge, ObjectNode element) {
        edge.metadata().entrySet()
                .stream()
                .filter(m -> accessor.relation$(edge.getLabel()).containsMetadata(m.getKey()))
                .forEach(m -> populate(mapper, accessor, indexProvider, context, element, m));
    }

    /**
     * metadata vertex populator
     *
     * @param context
     * @param node
     * @param element
     */
    public static void populateMetadataFields(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalNode node, ObjectNode element) {
        node.metadata().entrySet()
                .stream()
                .filter(m -> accessor.entity$(node.getLabel()).containsMetadata(m.getKey()))
                .forEach(m -> populate(mapper, accessor, indexProvider, context, element, m));
    }


    /**
     * fields vertex populator
     *
     * @param context
     * @param node
     * @param entity
     * @param element
     */
    public static void populateFields(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalNode node, Entity entity, ObjectNode element) {
        //todo check the structure of the index
        switch (entity.getMapping()) {
            case CHILD:
            case INDEX:
                //populate properties
                node.fields().entrySet()
                        .stream()
                        .filter(m -> accessor.entity$(node.getLabel()).containsProperty(m.getKey()))
                        .forEach(m -> populate(mapper, accessor, indexProvider, context, element, m));
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
    private ObjectNode populateFields(DataTransformerContext<LogicalGraphModel> context, LogicalEdge edge, Relation relation, String direction, ObjectNode element) {
        //populate redundant fields A
        switch (direction) {
            case "out":
                element.put(ENTITY_A, populateSide(ENTITY_A, context, edge.getSource(), relation));
                //populate redundant fields B
                element.put(ENTITY_B, populateSide(ENTITY_B, context, edge.getTarget(), relation));
                break;
            case "in":
                element.put(ENTITY_B, populateSide(ENTITY_A, context, edge.getSource(), relation));
                //populate redundant fields B
                element.put(ENTITY_A, populateSide(ENTITY_B, context, edge.getTarget(), relation));
                break;
        }

        //populate direct fields
        switch (relation.getMapping()) {
            case INDEX:
                //populate properties
                edge.fields().entrySet()
                        .stream()
                        .filter(m -> accessor.relation$(edge.getLabel()).containsProperty(m.getKey()))
                        .forEach(m -> populate(mapper,accessor,indexProvider,context, element, m));
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
    private ObjectNode populateSide(String side, DataTransformerContext<LogicalGraphModel> context, String sideId, Relation relation) {
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
                parseValue(redundant.getType(), o.toString(), sdf).toString()));
    }

    private Optional<LogicalNode> nodeById(DataTransformerContext<LogicalGraphModel> context, String id) {
        return context.getContainer().getNodes().stream().filter(n -> n.getId().equals(id)).findAny();
    }


}
