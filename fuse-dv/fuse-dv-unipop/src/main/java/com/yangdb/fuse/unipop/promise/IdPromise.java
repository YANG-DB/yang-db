package com.yangdb.fuse.unipop.promise;

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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Optional;

/**
 * Created by lior.perry on 07/03/2017.
 */
public class IdPromise implements Promise {
    //region Constructor
    public IdPromise(Object id) {
        this.id = id;
        this.label = Optional.empty();
    }

    public IdPromise(Object id, String label) {
        this(id);

        if (label == null || label.equals("")) {
            this.label = Optional.empty();
        } else {
            this.label = Optional.of(label);
        }
    }
    //endregion

    //region Promise Implementation
    @Override
    public Object getId() {
        return id;
    }
    //endregion

    //region Properties
    public Optional<String> getLabel() {
        return this.label;
    }
    //endregion

    //region Modulation
    public TraversalPromise by(Traversal traversal) {
        return new TraversalPromise(this.id, traversal);
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return label.isPresent() ?
                "Promise.as(" + getId().toString() + ", " + getLabel().get() + ")" :
                "Promise.as(" + getId().toString() + ")";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    //endregion

    //region fields
    private Object id;
    private Optional<String> label;
    //endregion
}
