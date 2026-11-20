package com.yangdb.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
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

import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.unipop.structure.discrete.DiscreteEdge;
import com.yangdb.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple3;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ImmutablePath;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * The forward-only cursor drops paths that revisit an element (KNOWN_ISSUES Q3: an edge between two entities that
 * share an id, e.g. Guild 0 -> Kingdom 0, was taken for a cycle and dropped).
 */
public class ForwardOnlyPathsTraversalCursorTest {

    private static Vertex vertex(String label, String id) {
        return new DiscreteVertex(id, label, null, Collections.emptyMap());
    }

    private static Path path(Vertex a, Vertex b, String edgeId) {
        DiscreteEdge edge = new DiscreteEdge(edgeId, "RegisteredIn", a, b, b, null, Collections.emptyMap());
        return ImmutablePath.make()
                .extend(a, Collections.singleton("A"))
                .extend(edge, Collections.singleton("A-->B"))
                .extend(b, Collections.singleton("B"));
    }

    private static List<String> results(Path... paths) {
        ForwardOnlyPathsTraversalCursor cursor = Mockito.mock(ForwardOnlyPathsTraversalCursor.class, Mockito.CALLS_REAL_METHODS);
        TraversalCursorContext context = Mockito.mock(TraversalCursorContext.class, Mockito.RETURNS_DEEP_STUBS);
        when(context.getTraversal().next(anyInt())).thenReturn(Arrays.asList(paths));
        when(context.getQueryResource().getQueryMetadata())
                .thenReturn(new com.yangdb.fuse.model.query.QueryMetadata(null, "q1", "q1", false, 0L, 0L));
        try {
            java.lang.reflect.Field field = com.yangdb.fuse.executor.cursor.BaseCursor.class.getDeclaredField("context");
            field.setAccessible(true);
            field.set(cursor, context);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        ETyped a = new ETyped(1, "A", "Guild", 2);
        ETyped b = new ETyped(3, "B", "Kingdom", 0);
        Rel rel = new Rel(2, "RegisteredIn", Rel.Direction.R, null, 3);
        rel.seteTag("r");
        Map<String, EEntityBase> entities = new HashMap<>();
        entities.put("A", a);
        entities.put("B", b);
        Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> rels = new HashMap<>();
        rels.put("A-->B", new Tuple3<>(a, rel, b));
        cursor.eEntityBases = entities;
        cursor.eRels = rels;
        cursor.includeEntities = true;
        cursor.includeRelationships = true;

        AssignmentsQueryResult<Entity, ?> result = cursor.toQuery(100);
        return result.getAssignments().stream()
                .map(assignment -> ((Assignment<Entity, ?>) assignment).getEntities().stream()
                        .map(e -> e.geteType() + ":" + e.geteID()).sorted().collect(Collectors.joining(",")))
                .sorted().collect(Collectors.toList());
    }

    @Test
    public void testRelationshipBetweenEntitiesSharingAnIdIsKept() {
        Assert.assertEquals(Collections.singletonList("Guild:0,Kingdom:0"),
                results(path(vertex("Guild", "0"), vertex("Kingdom", "0"), "RegisteredIn:r1")));
    }

    @Test
    public void testPathReturningToTheSameEntityIsDropped() {
        Vertex guild = vertex("Guild", "0");
        Assert.assertEquals(Collections.emptyList(), results(path(guild, guild, "RegisteredIn:r2")));
    }
}
