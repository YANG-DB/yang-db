package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.DataTransformerContext;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.projection.ProjectionAssignment;
import com.yangdb.fuse.model.projection.ProjectionEdge;
import com.yangdb.fuse.model.projection.ProjectionNode;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Relationship;

import java.util.List;
import java.util.stream.Collectors;

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
        context.withContainer(data.getAssignments().stream().map(a->buildAssignment(a)).collect(Collectors.toList()));
        return context;
    }

    private ProjectionAssignment buildAssignment(Assignment<Entity, Relationship> assignment) {
        ProjectionAssignment projection = new ProjectionAssignment(assignment.getId());
        projection.withAll(assignment.getEntities().stream().map(a->translate(a,assignment)).collect(Collectors.toList()));
        return projection;
    }

    private ProjectionNode translate(Entity entity, Assignment<Entity, Relationship> assignment) {
        ProjectionNode node = new ProjectionNode(entity.id(),entity.geteType());
        node.withTag(entity.tag());
        node.withProperties(entity.getProperties());

        assignment.getRelationBySource(entity.geteID())
                .forEach(rel -> node.withEdge(translate(rel,assignment)));
        return node;
    }

    private ProjectionEdge translate(Relationship relationship, Assignment<Entity, Relationship> assignment) {
        String targetLabel = assignment.getEntityById(relationship.geteID2()).get().label();
        return new ProjectionEdge(relationship.id(),relationship.tag(),relationship.label(),
                targetLabel,relationship.geteID2(),relationship.isDirectional());
    }
}
