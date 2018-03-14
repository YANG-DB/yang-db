package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class GraphHierarchyTraversalCursor implements Cursor {
    //region Constructors
    public GraphHierarchyTraversalCursor(Cursor cursor, Iterable<String> countTags) {
        this.cursor = cursor;

        this.fullGraph = new QueryResult();
        this.fullGraph.setAssignments(new ArrayList<>());
        this.fullGraph.getAssignments().add(new Assignment());
        this.fullGraph.getAssignments().get(0).setEntities(new ArrayList<>());
        this.fullGraph.getAssignments().get(0).setRelationships(new ArrayList<>());

        this.entityIds = new HashSet<>();
        this.relationshipIds = new HashSet<>();

        this.countTags = Stream.ofAll(countTags).toJavaSet();
        this.distinctIds = new HashSet<>();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        QueryResult newResult = this.cursor.getNextResults(numResults);
        while(newResult.getAssignments().size() > 0) {
            consolidateFullGraph(newResult);
            if (this.distinctIds.size() >= numResults) {
                break;
            }

            newResult = this.cursor.getNextResults(numResults);
        }

        this.fullGraph.getAssignments().get(0).setEntities(
                Stream.ofAll(this.fullGraph.getAssignments().get(0).getEntities())
                        .sortBy(Entity::geteType)
                        .toJavaList());

        return this.fullGraph;
    }
    //endregion

    //region Protected Methods


    private void consolidateFullGraph(QueryResult result) {
        Map<String, Stream<Entity>> newEntityStreams =
                Stream.ofAll(result.getAssignments())
                        .flatMap(Assignment::getEntities)
                        .filter(entity -> !this.entityIds.contains(entity.geteID()))
                        .groupBy(Entity::geteID).toJavaMap();

        Map<String, Entity> newEntities = Stream.ofAll(newEntityStreams.values())
                .map(entityStream -> {
                    Entity.Builder entityBuilder = Entity.Builder.instance();
                    Stream.ofAll(entityStream).forEach(entityBuilder::withEntity);
                    return entityBuilder.build();
                })
                .toJavaMap(entity -> new Tuple2<>(entity.geteID(), entity));

        this.distinctIds.addAll(Stream.ofAll(newEntities.values())
                .filter(newEntity -> Stream.ofAll(newEntity.geteTag())
                        .filter(tag -> this.countTags.contains(tag))
                        .toJavaOptional().isPresent())
                .map(Entity::geteID)
                .toJavaSet());

        Map<String, Relationship> newRelationships =
                Stream.ofAll(result.getAssignments())
                        .flatMap(Assignment::getRelationships)
                        .filter(relationship -> !this.relationshipIds.contains(relationship.getrID()))
                        .distinctBy(Relationship::getrID)
                        .toJavaMap(relationship -> new Tuple2<>(relationship.getrID(), relationship));

        this.fullGraph.getAssignments().get(0).getEntities().addAll(newEntities.values());
        this.fullGraph.getAssignments().get(0).getRelationships().addAll(newRelationships.values());

        this.entityIds.addAll(newEntities.keySet());
        this.relationshipIds.addAll(newRelationships.keySet());
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private QueryResult fullGraph;

    private Set<String> entityIds;
    private Set<String> relationshipIds;

    private Set<String> countTags;
    private Set<String> distinctIds;
    //endregion
}
