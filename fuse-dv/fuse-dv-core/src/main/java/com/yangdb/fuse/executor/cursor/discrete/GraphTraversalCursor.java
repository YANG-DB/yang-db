package com.yangdb.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.properties.CalculatedEProp;
import com.yangdb.fuse.model.query.properties.projection.CalculatedFieldProjection;
import com.yangdb.fuse.model.results.*;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

import static com.yangdb.fuse.executor.cursor.discrete.CalculatedFieldsUtil.findCalculaedFields;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class GraphTraversalCursor implements Cursor<TraversalCursorContext> {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new GraphTraversalCursor(new PathsTraversalCursor((TraversalCursorContext) context));
        }
        //endregion
    }
    //endregion

    //region Constructors
    public GraphTraversalCursor(Cursor<TraversalCursorContext> cursor) {
        this.cursor = cursor;

        this.fullGraph = new AssignmentsQueryResult<>();
        this.fullGraph.setAssignments(new ArrayList<>());
        this.fullGraph.getAssignments().add(new Assignment());
        this.fullGraph.getAssignments().get(0).setEntities(new ArrayList<>());
        this.fullGraph.getAssignments().get(0).setRelationships(new ArrayList<>());

        this.entityIds = new HashSet<>();
        this.entityTags = new HashSet<>();
        this.relationshipIds = new HashSet<>();
        this.relationshipTags = new HashSet<>();
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
    private void consolidateFullGraph(AssignmentsQueryResult<Entity, Relationship> result) {
        AsgQuery pattern = this.cursor.getContext().getQueryResource().getAsgQuery();

        //get unique entity tags
        Set<String> eTags = Stream.ofAll(result.getAssignments())
                .flatMap(Assignment::getEntities)
                .filter(entity -> !this.entityTags.contains(entity.geteTag()))
                .flatMap(e -> e.geteTag())
                .distinct()
                .toJavaSet();

        //add calculated fields of existing eTags
        Map<String, List<CalculatedEProp>> calculatedFieldsMap = Stream.ofAll(eTags)
                .toJavaMap(p -> new Tuple2<>(p, findCalculaedFields(pattern, p)));

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

        //count relations for specific pair of tags
        addCalculatedFields(calculatedFieldsMap, newEntities, newRelationships);

        this.fullGraph.getAssignments().get(0).getEntities().addAll(newEntities.values());
        this.fullGraph.getAssignments().get(0).getRelationships().addAll(newRelationships.values());

        this.fullGraph.getAssignments().get(0).setEntities(
                Stream.ofAll(this.fullGraph.getAssignments().get(0).getEntities())
                        .sortBy(Entity::geteType)
                        .toJavaList());

        this.entityIds.addAll(newEntities.keySet());
        this.relationshipIds.addAll(newRelationships.keySet());
    }

    private void addCalculatedFields(Map<String, List<CalculatedEProp>> calculatedFieldsMap, Map<String, Entity> newEntities, Map<String, Relationship> newRelationships) {
        //for each eTag name -> go over all its calculated fields definitions and...
        for (String entry : calculatedFieldsMap.keySet()) {
            //extract for each entityId a calculated number related to the specific calculated field
            calculatedFieldsMap.get(entry).forEach(field -> {
                Map<String, Integer> calculatedFieldsById = calculateFieldAgg(entry, field, newRelationships);
                calculatedFieldsById.entrySet().stream()
                        .filter(val -> newEntities.containsKey(val.getKey()))
                        //for each uniqueId in the relations -> add the calculated field with the calculated value
                        .forEach(val -> newEntities.get(val.getKey())
                                .setProperty(new Property(field.getProj().getExpression() +"["+ field.getpType()+"]", val.getValue())));
            });
        }
    }

    private Map<String, Integer> calculateFieldAgg(String eTag, CalculatedEProp prop, Map<String, Relationship> newRelationships) {
        return Stream.ofAll(newRelationships.values())
                .filter(v -> v.geteTag1().equals(eTag))
                .filter(v -> v.geteTag2().equals(prop.getpType()))
                .groupBy(Relationship::geteID1)
                .toJavaMap(p -> new Tuple2<>(p._1, agg(p._2.toJavaList(), prop.getProj())));
    }

    private int agg(List<Relationship> relationships, CalculatedFieldProjection con) {
        switch (con.getExpression()) {
            case count:
                return relationships.size();
            case max:
                //todo what ?!?!?
                return relationships.size();
            case min:
                //todo what ?!?!?
                return relationships.size();
            case avg:
                //todo what ?!?!?
                return relationships.size();
            case distinct:
                //todo what ?!?!?
                return relationships.size();
            case sum:
                //todo what ?!?!?
                return relationships.size();
            default:
                return relationships.size();
        }
    }

    @Override
    public TraversalCursorContext getContext() {
        return cursor.getContext();
    }

//endregion

    //region Fields
    private Cursor<TraversalCursorContext> cursor;
    private AssignmentsQueryResult<Entity, Relationship> fullGraph;

    private Set<String> entityIds;
    private Set<String> entityTags;
    private Set<String> relationshipIds;
    private Set<String> relationshipTags;
    //endregion
}
