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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The paths cursors drop duplicate assignments; entities of different types sharing an id are not duplicates
 * (an untyped {@code match (p)} returned one entity per id: Dragon 7 hid Horse 7 and Person 7).
 */
public class AssignmentsQueryResultDistinctTest {

    private static Assignment<Entity, Relationship> single(String type, String id) {
        Entity entity = Entity.Builder.instance().withEType(type).withEID(id)
                .withETag(new HashSet<>(Collections.singleton("p"))).build();
        return Assignment.Builder.instance().withEntity(entity).build();
    }

    private static List<String> distinct(Assignment<Entity, Relationship>... assignments) {
        AssignmentsQueryResult.Builder builder = AssignmentsQueryResult.Builder.instance();
        for (Assignment<Entity, Relationship> assignment : assignments) {
            builder.withAssignment(assignment);
        }
        AssignmentsQueryResult<Entity, Relationship> result = AssignmentsQueryResult.distinct(builder.build());
        return result.getAssignments().stream()
                .map(a -> a.getEntities().get(0).geteType() + ":" + a.getEntities().get(0).geteID())
                .sorted().collect(Collectors.toList());
    }

    @Test
    public void testSameIdDifferentTypesAreKept() {
        Assert.assertEquals(java.util.Arrays.asList("Dragon:7", "Horse:7", "Person:7"),
                distinct(single("Dragon", "7"), single("Horse", "7"), single("Person", "7")));
    }

    @Test
    public void testRepeatedEntityIsDropped() {
        Assert.assertEquals(Collections.singletonList("Dragon:7"), distinct(single("Dragon", "7"), single("Dragon", "7")));
    }
}
