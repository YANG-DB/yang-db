package com.yangdb.fuse.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by roman.margolis on 10/10/2017.
 */
public interface GraphElementConstraint {
    Traversal getTraversalConstraint();

    class Impl implements GraphElementConstraint {
        //region Constructors
        public Impl(Traversal traversalConstraint) {
            this.traversalConstraint = traversalConstraint;
        }
        //endregion

        //region GraphElementConstraint Implementation
        @Override
        public Traversal getTraversalConstraint() {
            return this.traversalConstraint;
        }
        //endregion

        //region Fields
        private Traversal traversalConstraint;
        //endregion
    }
}
