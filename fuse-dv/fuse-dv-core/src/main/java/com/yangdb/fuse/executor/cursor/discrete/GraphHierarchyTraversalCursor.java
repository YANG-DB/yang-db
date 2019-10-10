package com.yangdb.fuse.executor.cursor.discrete;

/*-
 *
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Relationship;
import com.yangdb.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class GraphHierarchyTraversalCursor implements Cursor<TraversalCursorContext> {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new GraphHierarchyTraversalCursor(
                    new PathsTraversalCursor((TraversalCursorContext)context),
                    ((CreateGraphHierarchyCursorRequest)context.getCursorRequest()).getCountTags());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public GraphHierarchyTraversalCursor(Cursor<TraversalCursorContext> cursor, Iterable<String> countTags) {
        this.cursor = cursor;

        this.fullGraph = new AssignmentsQueryResult();
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
    public AssignmentsQueryResult getNextResults(int numResults) {
        AssignmentsQueryResult newResult = (AssignmentsQueryResult) this.cursor.getNextResults(numResults);
        while(newResult.getAssignments().size() > 0) {
            consolidateFullGraph(newResult);
            if (this.distinctIds.size() >= numResults) {
                break;
            }

            newResult = (AssignmentsQueryResult) this.cursor.getNextResults(numResults);
        }

        this.fullGraph.getAssignments().get(0).setEntities(
                Stream.ofAll(this.fullGraph.getAssignments().get(0).getEntities())
                        .sortBy(Entity::geteType)
                        .toJavaList());

        return this.fullGraph;
    }
    //endregion

    //region Protected Methods


    private void consolidateFullGraph(AssignmentsQueryResult<Entity,Relationship> result) {
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

    @Override
    public TraversalCursorContext getContext() {
        return cursor.getContext();
    }

    //endregion

    //region Fields
    private Cursor<TraversalCursorContext> cursor;
    private AssignmentsQueryResult<Entity,Relationship> fullGraph;

    private Set<String> entityIds;
    private Set<String> relationshipIds;

    private Set<String> countTags;
    private Set<String> distinctIds;
    //endregion
}
