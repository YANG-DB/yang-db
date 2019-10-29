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

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by benishue on 27-Mar-17.
 */
public class TraversalValuesByKeyProvider implements TraversalValueByKeyProvider<Set<String>> {

    @Override
    public Set<String> getValueByKey(Traversal traversal, String key) {
        TraversalValuesByKeyProvider.Visitor visitor = new TraversalValuesByKeyProvider.Visitor(key);
        visitor.visit(traversal);
        return visitor.getValues();
    }


    //region Visitor
    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructor
        public Visitor(String key) {
            this.values = new HashSet<>();
            this.key = key;
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitHasStep(HasStep<?> hasStep) {
            Stream.ofAll(hasStep.getHasContainers()).flatMap(hasContainer ->
            {
                if (this.key.equals(hasContainer.getKey()))
                {
                    if (Iterable.class.isAssignableFrom(hasContainer.getValue().getClass())) {
                        return Stream.ofAll(((Iterable) hasContainer.getValue())).map(Object::toString).toJavaList();
                    }
                    else if (String[].class.isAssignableFrom(hasContainer.getValue().getClass())) {
                        return Stream.of((String[]) hasContainer.getValue());
                    }
                    else {
                        return Stream.of(hasContainer.getValue());
                    }
                }
                else
                    return Stream.empty();
            }).forEach(value -> this.values.add(value.toString()));

            return Boolean.TRUE;
        }
        //endregion

        //region Properties
        public Set<String> getValues() {
            return this.values;
        }
        //endregion

        //region Fields
        private Set<String> values;
        private String key;
        //endregion
    }
    //endregion
}
