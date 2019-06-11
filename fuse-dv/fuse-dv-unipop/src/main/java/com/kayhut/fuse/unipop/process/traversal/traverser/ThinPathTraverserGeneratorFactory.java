package com.kayhut.fuse.unipop.process.traversal.traverser;

/*-
 * #%L
 * fuse-dv-unipop
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

import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserGeneratorFactory;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;

import java.util.Set;

/**
 * Created by Roman on 1/29/2018.
 */
public class ThinPathTraverserGeneratorFactory implements TraverserGeneratorFactory {
    //region TraverserGeneratorFactory Implementation
    @Override
    public TraverserGenerator getTraverserGenerator(Set<TraverserRequirement> set) {
        return new ThinPathTraverserGenerator();
    }
    //endregion
}
