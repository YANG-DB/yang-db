package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.DataTransformerContext;
import com.yangdb.fuse.executor.ontology.schema.load.DocumentBuilder;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.projection.ProjectionAssignment;
import com.yangdb.fuse.model.projection.ProjectionEdge;
import com.yangdb.fuse.model.projection.ProjectionNode;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Relationship;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.executor.ontology.DataTransformer.Utils.sdf;
import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.parseValue;
import static com.yangdb.fuse.model.GlobalConstants.EdgeSchema.DEST_ID;
import static com.yangdb.fuse.model.GlobalConstants.EdgeSchema.DEST_TYPE;
import static com.yangdb.fuse.model.GlobalConstants.ID;
import static com.yangdb.fuse.model.GlobalConstants.ProjectionConfigs.TAG;
import static com.yangdb.fuse.model.GlobalConstants.TYPE;

/**
 * transforms a single assignment (row) into a projection document (containing list of nodes and within each node a list of first hop edges)
 */
public class ProjectionTransformer implements DataTransformer<DataTransformerContext<List<ProjectionAssignment>>, AssignmentsQueryResult<Entity, Relationship>> {
    private static ObjectMapper mapper = new ObjectMapper();
    private final Ontology.Accessor accessor;


    public ProjectionTransformer(Ontology.Accessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public DataTransformerContext<List<ProjectionAssignment>> transform(AssignmentsQueryResult<Entity, Relationship> data, GraphDataLoader.Directive directive) {
        DataTransformerContext<List<ProjectionAssignment>> context = new DataTransformerContext<>(mapper);
        context.withContainer(data.getAssignments().stream().map(a -> buildAssignment(data.getQueryId(), data.getTimestamp(), a)).collect(Collectors.toList()));
        translate(context);
        return context;
    }

    private void translate(DataTransformerContext<List<ProjectionAssignment>> context) {
        List<ProjectionAssignment> container = context.getContainer();
        //translate assignments to root level entities - all relations are embedded inside the entities as nested document
        context.withEntities(container.stream().map(this::translate).collect(Collectors.toList()));
    }

    private DocumentBuilder translate(ProjectionAssignment row) {
        ObjectNode rootEntity = mapper.createObjectNode();
        //root level metadata
        rootEntity.put(TYPE, row.getType());
        rootEntity.put(GlobalConstants.ProjectionConfigs.QUERY_ID, row.getQueryId());
        rootEntity.put(GlobalConstants.ProjectionConfigs.EXECUTION_TIME, row.getTimestamp());
        row.getNodes().forEach(node -> translate(rootEntity, node));

        return new DocumentBuilder(rootEntity, String.valueOf(row.getId()), row.getType(), Optional.empty());
    }

    private void translate(ObjectNode root, ProjectionNode node) {
        if (root.get(node.label()) == null) {
            //create the specific node array for the internal nested document
            root.put(node.label(), mapper.createArrayNode());
        }
        com.fasterxml.jackson.databind.node.ArrayNode arrayNode = (ArrayNode) root.get(node.label());

        ObjectNode element = mapper.createObjectNode();
        //node level metadata
        element.put(ID, node.getId());
        element.put(TYPE, node.getLabel());
        element.put(TAG, node.tag());
        //node properties
        node.getProperties().getProperties().forEach((key, value) -> populateField(key, value, element));
        //node inner edges which are nested documents
        node.getProjectionEdge().forEach(edge -> translate(element, edge));
        arrayNode.add(element);
    }

    private void translate(ObjectNode rootNode, ProjectionEdge edge) {
        if (rootNode.get(edge.label()) == null) {
            //create the specific node array for the internal nested document
            rootNode.put(edge.label(), mapper.createArrayNode());
        }
        com.fasterxml.jackson.databind.node.ArrayNode arrayEdge = (ArrayNode) rootNode.get(edge.label());

        ObjectNode element = mapper.createObjectNode();
        //edge level metadata
        element.put(ID, edge.getId());
        element.put(TYPE, edge.getLabel());
        element.put(TAG, edge.tag());
        element.put(DEST_TYPE, edge.getTargetLabel());
        element.put(DEST_ID, edge.getTargetLabel());
        //edge properties
        edge.getProperties().getProperties().forEach((key, value) -> populateField(key, value, element));
        arrayEdge.add(element);
    }

    private void populateField(String key, Object value, ObjectNode element) {
        String pType = accessor.property$(key).getpType();
        Object result = parseValue(accessor.property$(key).getType(), value, sdf);
        //all primitive non string types
        element.put(pType, result.toString());
    }

    private ProjectionAssignment buildAssignment(String queryId, long timestamp, Assignment<Entity, Relationship> assignment) {
        ProjectionAssignment projection = new ProjectionAssignment(assignment.getId(), queryId, timestamp);
        projection.withAll(assignment.getEntities().stream().map(a -> translate(a, assignment)).collect(Collectors.toList()));
        return projection;
    }

    private ProjectionNode translate(Entity entity, Assignment<Entity, Relationship> assignment) {
        ProjectionNode node = new ProjectionNode(entity.id(), entity.geteType());
        node.withTag(entity.tag());
        node.withProperties(entity.getProperties());

        assignment.getRelationBySource(entity.geteID())
                .forEach(rel -> node.withEdge(translate(rel, assignment)));
        return node;
    }

    private ProjectionEdge translate(Relationship relationship, Assignment<Entity, Relationship> assignment) {
        String targetLabel = assignment.getEntityById(relationship.geteID2()).get().label();
        return new ProjectionEdge(relationship.id(), relationship.tag(), relationship.label(),
                targetLabel, relationship.geteID2(), relationship.isDirectional());
    }
}
