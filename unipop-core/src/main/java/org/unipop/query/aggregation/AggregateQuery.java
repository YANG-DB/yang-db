package org.unipop.query.aggregation;

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
 * AggregateQuery.java - unipop-core - yangdb - 2,016
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

import org.unipop.process.group.traversal.SemanticKeyTraversal;
import org.unipop.process.group.traversal.SemanticReducerTraversal;
import org.unipop.process.group.traversal.SemanticValuesTraversal;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.UniQueryController;
import org.unipop.query.predicates.PredicateQuery;

import java.util.Map;

public class AggregateQuery extends PredicateQuery {
    private final SemanticKeyTraversal key;
    private final SemanticValuesTraversal values;
    private final SemanticReducerTraversal reduce;

    public AggregateQuery(PredicatesHolder predicates,
                          SemanticKeyTraversal key,
                          SemanticValuesTraversal values,
                          SemanticReducerTraversal reduce,
                          StepDescriptor stepDescriptor) {
        super(predicates, stepDescriptor);
        this.key = key;
        this.values = values;
        this.reduce = reduce;
    }

    public SemanticKeyTraversal getKey() {
        return key;
    }

    public SemanticValuesTraversal getValues() {
        return values;
    }

    public SemanticReducerTraversal getReduce() {
        return reduce;
    }

    public interface AggregationController extends UniQueryController {
        Map<String, Object> query(AggregateQuery uniQuery);
    }

    @Override
    public String toString() {
        return "AggregateQuery{" +
                "key=" + key +
                ", values=" + values +
                ", reduce=" + reduce +
                '}';
    }
}
