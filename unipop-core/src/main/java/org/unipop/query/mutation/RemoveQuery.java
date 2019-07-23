package org.unipop.query.mutation;

/*-
 * #%L
 * RemoveQuery.java - unipop-core - yangdb - 2,016
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
import org.unipop.query.StepDescriptor;
import org.unipop.query.UniQuery;
import org.unipop.query.controller.UniQueryController;

import java.util.List;

public class RemoveQuery<E extends Element> extends UniQuery {
    private final List<E> elements;

    public RemoveQuery(List<E> elements, StepDescriptor stepDescriptor) {
        super(stepDescriptor);
        this.elements = elements;
    }

    public List<E> getElements(){
        return elements;
    }

    public interface RemoveController extends UniQueryController {
        <E extends Element>void remove(RemoveQuery<E> uniQuery);
    }

    @Override
    public String toString() {
        return "RemoveQuery{" +
                "elements=" + elements +
                '}';
    }
}
