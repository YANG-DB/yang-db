package org.unipop.process.start;

/*-
 * #%L
 * unipop-core
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

/*-
 *
 * UniGraphOrderedTraversersInjectStep.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.*;

public class UniGraphOrderedTraversersInjectStep<S> extends AbstractStep<S, S> {
    //region Constructors
    public UniGraphOrderedTraversersInjectStep(Traversal.Admin traversal, List<Traverser.Admin<S>> starts) {
        super(traversal);

        this.starts = new ArrayDeque<>(starts);
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<S> processNextStart() throws NoSuchElementException {
        if (this.starts.isEmpty()) {
            throw FastNoSuchElementException.instance();
        }

        return this.starts.remove();
    }

    @Override
    public void addStarts(final Iterator<org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin<S>> starts) {
        this.starts.addAll(Stream.ofAll(() -> starts).toJavaList());
    }

    @Override
    public void addStart(final org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin<S> start) {
        this.starts.add(start);
    }

    @Override
    public void reset() {
        super.reset();
        this.starts.clear();
    }
    //endregion

    //region Fields
    private Queue<Traverser.Admin<S>> starts;
    //endregion
}
