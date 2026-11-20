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

import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.Relationship;
import javaslang.Tuple3;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ImmutablePath;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Relationship end tags for an undirected pattern {@code (A)-[r]-(B)} (KNOWN_ISSUES Q1): eID1/eID2 are the stored
 * out/in vertices, so eTag1/eTag2 must name whichever pattern entity each of them is.
 */
public class PathsTraversalCursorTest {

    private static Relationship undirectedRelationship(Vertex a, Vertex b, Vertex out, Vertex in) {
        PathsTraversalCursor cursor = Mockito.mock(PathsTraversalCursor.class, Mockito.CALLS_REAL_METHODS);
        ETyped eA = new ETyped(1, "A", "Dragon", 2);
        ETyped eB = new ETyped(3, "B", "Dragon", 0);
        Rel rel = new Rel(2, "Fire", Rel.Direction.RL, null, 3);
        rel.seteTag("r");

        Map<String, EEntityBase> entities = new HashMap<>();
        entities.put("A", eA);
        entities.put("B", eB);
        Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> rels = new HashMap<>();
        rels.put("A--B", new Tuple3<>(eA, rel, eB));
        cursor.eEntityBases = entities;
        cursor.eRels = rels;
        cursor.includeEntities = true;
        cursor.includeRelationships = true;

        DetachedEdge.Builder edgeBuilder = DetachedEdge.build();
        edgeBuilder.setId("Fire:f1");
        edgeBuilder.setLabel("Fire");
        edgeBuilder.setOutV((DetachedVertex) out);
        edgeBuilder.setInV((DetachedVertex) in);
        DetachedEdge edge = edgeBuilder.create();
        Path path = ImmutablePath.make()
                .extend(a, Collections.singleton("A"))
                .extend(edge, Collections.singleton("A--B"))
                .extend(b, Collections.singleton("B"));

        Assignment assignment = cursor.toAssignment(path);
        Assert.assertEquals(1, assignment.getRelationships().size());
        return (Relationship) assignment.getRelationships().get(0);
    }

    private static Vertex dragon(String id) {
        return DetachedVertex.build().setId(id).setLabel("Dragon").create();
    }

    @Test
    public void testUndirectedRelationshipStoredFromAToB() {
        Vertex d4 = dragon("4"), d465 = dragon("465");
        Relationship r = undirectedRelationship(d4, d465, d4, d465);
        Assert.assertEquals("4", r.geteID1());
        Assert.assertEquals("465", r.geteID2());
        Assert.assertEquals("A", r.geteTag1());
        Assert.assertEquals("B", r.geteTag2());
    }

    @Test
    public void testUndirectedRelationshipStoredFromBToA() {
        Vertex d465 = dragon("465"), d4 = dragon("4");
        // pattern entity A is Dragon 465, but the stored relationship is 4 -> 465
        Relationship r = undirectedRelationship(d465, d4, d4, d465);
        Assert.assertEquals("4", r.geteID1());
        Assert.assertEquals("465", r.geteID2());
        Assert.assertEquals("B", r.geteTag1());
        Assert.assertEquals("A", r.geteTag2());
    }
}
