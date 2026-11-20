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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entities of different types may share an id (KNOWN_ISSUES Q3: Person 22 owns Dragon 22); an assignment must keep both.
 */
public class AssignmentBuilderTest {

    private static Entity entity(String type, String id, String tag) {
        // mutable tag set, as the cursors build it (merging adds tags)
        return Entity.Builder.instance().withEType(type).withEID(id)
                .withETag(new java.util.HashSet<>(java.util.Collections.singleton(tag))).build();
    }

    @Test
    public void testEntitiesOfDifferentTypesWithTheSameIdAreKept() {
        Assignment<Entity, Relationship> assignment = Assignment.Builder.instance()
                .withEntity(entity("Person", "22", "A"))
                .withEntity(entity("Dragon", "22", "B"))
                .build();

        List<String> entities = assignment.getEntities().stream()
                .map(e -> e.geteType() + ":" + e.geteID() + ":" + e.geteTag())
                .sorted().collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("Dragon:22:[B]", "Person:22:[A]"), entities);
    }

    @Test
    public void testSameEntityReachedTwiceIsMerged() {
        Assignment<Entity, Relationship> assignment = Assignment.Builder.instance()
                .withEntity(entity("Dragon", "7", "A"))
                .withEntity(entity("Dragon", "7", "B"))
                .build();

        Assert.assertEquals(1, assignment.getEntities().size());
        Assert.assertEquals(2, assignment.getEntities().get(0).geteTag().size());
    }
}
