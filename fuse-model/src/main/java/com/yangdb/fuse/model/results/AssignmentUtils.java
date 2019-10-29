package com.yangdb.fuse.model.results;

/*-
 * #%L
 * fuse-model
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

import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AssignmentUtils {

    public static final String ID = "id";//id projected entityField = "eID"

    /**
     * find entity.field tag name in query result assignments and collect them to an set
     *
     * @param result
     * @param tag
     * @return
     */
    public static NamedParameter collectByTag(AssignmentsQueryResult<Entity,Relationship> result, String tag) {
        Set results = new LinkedHashSet<>();
        String[] split = tag.split("[.]");
        //eTag
        String entityTag = split[0];
        if ((split.length == 2) && !split[1].equals(ID)) {
            String entityField = split[1];
            result.getAssignments().forEach(
                    assignment -> results.addAll(assignment.getEntities().stream()
                            .filter(ent -> ent.geteTag().contains(entityTag))
                            .filter(ent -> ent.getProperty(entityField).isPresent())
                            .map(ent -> ent.getProperty(entityField))
                            .filter(Optional::isPresent)
                            .map(p -> p.get().getValue())
                            .collect(Collectors.toList())));
        } else {
            //default id projected entityField = "eID"
            result.getAssignments().forEach(
                    assignment -> results.addAll(assignment.getEntities().stream()
                            .filter(ent -> ent.geteTag().contains(entityTag))
                            .map(Entity::geteID)
                            .collect(Collectors.toList())));
        }
        return new NamedParameter(tag, results);
    }
}
