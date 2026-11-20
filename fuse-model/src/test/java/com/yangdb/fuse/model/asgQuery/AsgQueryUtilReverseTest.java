package com.yangdb.fuse.model.asgQuery;

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

import com.yangdb.fuse.model.query.Rel;
import org.junit.Assert;
import org.junit.Test;

/**
 * The planner walks a pattern from either end and reverses relations it crosses backwards (KNOWN_ISSUES Q1:
 * an undirected relation reversed into a directed one).
 */
public class AsgQueryUtilReverseTest {

    private static Rel.Direction reversed(Rel.Direction direction) {
        Rel rel = new Rel(2, "Know", direction, "know", 3);
        return AsgQueryUtil.reverse(AsgEBase.Builder.<Rel>get().withEBase(rel).build()).geteBase().getDir();
    }

    @Test
    public void testUndirectedStaysUndirected() {
        Assert.assertEquals(Rel.Direction.RL, reversed(Rel.Direction.RL));
    }

    @Test
    public void testDirectedIsFlipped() {
        Assert.assertEquals(Rel.Direction.L, reversed(Rel.Direction.R));
        Assert.assertEquals(Rel.Direction.R, reversed(Rel.Direction.L));
    }
}
