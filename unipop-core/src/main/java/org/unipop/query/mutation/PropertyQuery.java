package org.unipop.query.mutation;

/*-
 * #%L
 * PropertyQuery.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.StepDescriptor;
import org.unipop.query.UniQuery;
import org.unipop.query.controller.UniQueryController;

public class PropertyQuery<E extends Element>  extends UniQuery {
    private final E element;
    private final Property property;
    private final Action action;

    public PropertyQuery(E element, Property property, Action action, StepDescriptor stepDescriptor) {
        super(stepDescriptor);
        this.element = element;
        this.property = property;
        this.action = action;
    }

    public enum Action {
        Add,
        Remove
    }

    public Action getAction(){
        return action;
    }

    public E getElement() {
        return element;
    }

    public Property getProperty(){
        return property;
    }

    public interface PropertyController extends UniQueryController {
        <E extends Element> void property(PropertyQuery<E> uniQuery);
    }

    @Override
    public String toString() {
        return "PropertyQuery{" +
                "element=" + element +
                ", property=" + property +
                ", action=" + action +
                '}';
    }
}
