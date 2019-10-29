package com.yangdb.fuse.unipop.controller.utils.traversal;

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

import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;

public class TraversalExactProvider implements TraversalValueProvider<Traversal> {
    //region TraversalValueProvider Implementation
    @Override
    public Traversal getValue(Traversal traversal) {
        Traversal clone = traversal.asAdmin().clone();
        new Visitor().visit(clone);
        return clone;
    }
    //endregion

    //region Visitor
    private static class Visitor extends TraversalVisitor<Traversal> {
        //region TraversalVisitor Implementation
        @Override
        protected Traversal visitNotStep(NotStep<?> notStep) {
            notStep.getTraversal().asAdmin().removeStep(notStep);
            return null;
        }

        @Override
        protected Traversal visitHasStep(HasStep<?> hasStep) {
            if (!isExactPredicate( hasStep.getHasContainers().get(0).getPredicate())) {
                hasStep.getTraversal().asAdmin().removeStep(hasStep);
            }

            return null;
        }
        //endregion

        //region Private Methods
        private boolean isExactPredicate(P<?> predicate) {
            if ((predicate.getBiPredicate() instanceof Compare)) {
                Compare compare = (Compare)predicate.getBiPredicate();
                if (compare.equals(Compare.eq)) {
                    return true;
                }
            }

            if ((predicate.getBiPredicate() instanceof Contains)) {
                Contains contains = (Contains)predicate.getBiPredicate();
                if (contains.equals(Contains.within)) {
                    return true;
                }
            }

            return false;
        }
        //endregion
    }
    //endregion
}
