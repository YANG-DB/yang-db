package org.unipop.process.traversal;

/*-
 * #%L
 * QueueExpandableStepIterator.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ExpandableStepIterator;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.TraverserSet;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.*;

/*public class QueueExpandableStepIterator<S> extends ExpandableStepIterator<S> {
    //region Constructors
    public QueueExpandableStepIterator(Step<S, ?> hostStep) {
        super(hostStep);
        this.traversers = new ArrayDeque<>();
    }
    //endregion

    //region ExpandableStepIterator Implementation
    @Override
    public boolean hasNext() {
        return !this.traversers.isEmpty() || this.hostStep.getPreviousStep().hasNext();
    }

    @Override
    public Traverser.Admin<S> next() {
        if (!this.traversers.isEmpty()) {
            return this.traversers.remove();
        } else {
            if (this.hostStep.getPreviousStep().hasNext()) {
                return this.hostStep.getPreviousStep().next();
            }

            if (!this.traversers.isEmpty()) {
                return this.traversers.remove();
            }

            throw FastNoSuchElementException.instance();
        }
    }

    @Override
    public void add(final Iterator<Traverser.Admin<S>> iterator) {
        iterator.forEachRemaining(traverser -> this.traversers.add(traverser));
    }

    @Override
    public void add(final Traverser.Admin<S> traverser) {
        this.traversers.add(traverser);
    }

    @Override
    public String toString() {
        return this.traversers.toString();
    }

    @Override
    public void clear() {
        this.traversers.clear();
    }
    //endregion

    //region Fields
    private Queue<Traverser.Admin<S>> traversers;
    //endregion
}*/
