package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class GraphTraversalCursor implements Cursor {
    //region Constructors
    public GraphTraversalCursor(Cursor cursor) {
        this.cursor = cursor;

        this.fullGraph = new QueryResult();
        this.fullGraph.setAssignments(new ArrayList<>());
        this.fullGraph.getAssignments().add(new Assignment());
        this.fullGraph.getAssignments().get(0).setEntities(new ArrayList<>());
        this.fullGraph.getAssignments().get(0).setRelationships(new ArrayList<>());

        this.entityIds = new HashSet<>();
        this.relationshipIds = new HashSet<>();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        QueryResult newResult = this.cursor.getNextResults(numResults);
        consolidateFullGraph(newResult);

        return this.fullGraph;
    }
    //endregion

    //region Protected Methods
    private void consolidateFullGraph(QueryResult result) {
        Map<String, Entity> newEntities =
                Stream.ofAll(result.getAssignments())
                .flatMap(Assignment::getEntities)
                .filter(entity -> !this.entityIds.contains(entity.geteID()))
                .distinctBy(Entity::geteID)
                .toJavaMap(entity -> new Tuple2<>(entity.geteID(), entity));

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
    //endregion
}
