package com.kayhut.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class GraphTraversalCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new GraphTraversalCursor(new PathsTraversalCursor((TraversalCursorContext)context));
        }
        //endregion
    }
    //endregion

    //region Constructors
    public GraphTraversalCursor(Cursor cursor) {
        this.cursor = cursor;

        this.fullGraph = new AssignmentsQueryResult<Entity,Relationship>();
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
    public AssignmentsQueryResult getNextResults(int numResults) {
        AssignmentsQueryResult newResult = (AssignmentsQueryResult) this.cursor.getNextResults(numResults);
        consolidateFullGraph(newResult);

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

        Map<String, Relationship> newRelationships =
                Stream.ofAll(result.getAssignments())
                .flatMap(Assignment::getRelationships)
                .filter(relationship -> !this.relationshipIds.contains(relationship.getrID()))
                .distinctBy(Relationship::getrID)
                .toJavaMap(relationship -> new Tuple2<>(relationship.getrID(), relationship));

        this.fullGraph.getAssignments().get(0).getEntities().addAll(newEntities.values());
        this.fullGraph.getAssignments().get(0).getRelationships().addAll(newRelationships.values());

        this.fullGraph.getAssignments().get(0).setEntities(
                Stream.ofAll(this.fullGraph.getAssignments().get(0).getEntities())
                        .sortBy(Entity::geteType)
                        .toJavaList());

        this.entityIds.addAll(newEntities.keySet());
        this.relationshipIds.addAll(newRelationships.keySet());
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private AssignmentsQueryResult<Entity,Relationship> fullGraph;

    private Set<String> entityIds;
    private Set<String> relationshipIds;
    //endregion
}
