package org.unipop.util.iterator;

/*-
 * #%L
 * ConcatIterator.java - unipop-core - kayhut - 2,016
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

import java.util.Collections;
import java.util.Iterator;

public class ConcatIterator<T> implements Iterator<T> {

    private final Iterator<? extends Iterator<? extends T>> iterators;
    private Iterator<? extends T> current;

    public ConcatIterator(Iterator<? extends Iterator<? extends T>> iterators) {
        this.current = Collections.emptyIterator();
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        while (!current.hasNext() && iterators.hasNext()) {
            current = iterators.next();
        }
        return current.hasNext();
    }

    @Override
    public T next() {
        return current.next();
    }
}
