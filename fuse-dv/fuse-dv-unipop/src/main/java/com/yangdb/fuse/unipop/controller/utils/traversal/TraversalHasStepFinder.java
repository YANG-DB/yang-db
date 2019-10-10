package com.yangdb.fuse.unipop.controller.utils.traversal;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Roman on 9/17/2017.
 */
public class TraversalHasStepFinder implements TraversalValueProvider<Iterable<HasStep>> {
    //region Constructors
    public TraversalHasStepFinder(Predicate<HasStep<?>> hasStepPredicate) {
        this.hasStepPredicate = hasStepPredicate;
    }
    //endregion

    //region TraversalValueProvider Implementation
    @Override
    public Iterable<HasStep> getValue(Traversal traversal) {
        Visitor visitor = new Visitor(this.hasStepPredicate);
        visitor.visit(traversal);
        return visitor.getHasSteps();
    }
    //endregions

    //region Fields
    private Predicate<HasStep<?>> hasStepPredicate;
    //endregion

    //region Visitor
    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructors
        public Visitor(Predicate<HasStep<?>> predicate) {
            this.predicate = predicate;
            this.hasSteps = new HashSet<>();
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitHasStep(HasStep<?> hasStep) {
            if (this.predicate.test(hasStep)) {
                this.hasSteps.add(hasStep);
                return Boolean.TRUE;
            }

            return Boolean.FALSE;
        }
        //endregion

        //region Properties
        public Iterable<HasStep> getHasSteps() {
            return this.hasSteps;
        }
        //endregion

        //region Fields
        private Predicate<HasStep<?>> predicate;
        private Set<HasStep> hasSteps;
        //endregion
    }
    //endregion
}
