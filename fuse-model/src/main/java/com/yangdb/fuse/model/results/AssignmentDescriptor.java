package com.yangdb.fuse.model.results;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.descriptors.Descriptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.StringJoiner;

public class AssignmentDescriptor implements Descriptor<Assignment<Entity, Relationship>> {
    @Override
    public String describe(Assignment<Entity, Relationship> item) {
        return patternValue(item);
    }

    //region Private Methods
    private String patternValue(Assignment<Entity, Relationship> assignment) {
        StringJoiner joiner = new StringJoiner("-", "", "");

        assignment.getRelationships().stream().sorted()
                .forEach(rel -> {
            joiner.add(print(getEntityById(rel.geteID1(), assignment).orElseGet(() ->
                    new Entity(Collections.emptySet(), rel.geteID1(),"???", Collections.emptyMap() ))));
            joiner.add(print(rel));
            joiner.add(print(getEntityById(rel.geteID2(), assignment).orElseGet(() ->
                    new Entity(Collections.emptySet(), rel.geteID1(),"???", Collections.emptyMap() ))));
        });
        return (joiner.toString().toCharArray()[joiner.length() - 1] == '-') ?
                joiner.toString().substring(0, joiner.length() - 1) : joiner.toString();

    }

    private Optional<Entity> getEntityById(String id, Assignment<Entity, Relationship> assignment) {
        return assignment.getEntities().stream().filter(e -> e.geteID().equals(id)).findFirst();
    }

    public static String print(Assignment<Entity, Relationship> item) {
        return new AssignmentDescriptor().patternValue(item);
    }

    private static String print(Entity entity) {
        return String.format("[%s,%s, %s]", entity.geteID(), strip(String.join(",", entity.geteTag())), entity.geteType());
    }

    private static String print(Relationship relationship) {
        return String.format("(%s, %s)", relationship.getrID(), relationship.getrType());
    }

    private static String strip(String value) {
        return value.replace('[', ' ').replace(']', ' ');
    }
}

