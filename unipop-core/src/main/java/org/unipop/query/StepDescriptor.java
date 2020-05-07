package org.unipop.query;

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
 * StepDescriptor.java - unipop-core - yangdb - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.unipop.process.start.UniGraphStartStep;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class StepDescriptor {

    public static final String STEP_DESCRIPTOR_PREFIX = "_";

    private MutableMetrics metrics;
    private Step step;

    public StepDescriptor(Step step) {
        this.step = step;
    }

    public <S, E extends Element> StepDescriptor(Step<S, E> step, MutableMetrics metrics) {
        this(step);
        this.metrics = metrics;
    }

    public String getId(){
        return step.getId();
    }

    public Set<String> getLabels(){
        return step.getLabels();
    }

    public String getDescription(){
       return (String) step.getLabels().stream()
               .filter(l->l.toString().startsWith(STEP_DESCRIPTOR_PREFIX))
               .max(Comparator.comparingInt(String::length)).get();
    }

    public Optional<MutableMetrics> getMetrics(){
        return Optional.ofNullable(metrics);
    }

    @Override
    public String toString() {
        return step.toString()
                + " { ID: " + step.getId()
                + " }";
    }
}
