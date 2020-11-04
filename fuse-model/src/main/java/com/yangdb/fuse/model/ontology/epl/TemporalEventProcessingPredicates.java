package com.yangdb.fuse.model.ontology.epl;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.quant.Quant1;

/**
 * this event processing semantic language operator is used to define
 * a relation between elements in the query which is not related to the ontology structure but to the occurrence of
 * the instances of the subjective parts
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TemporalEventProcessingPredicates extends Quant1 {
    private Semantics semantics;
    private Constraint constraint;
    private String tag;

    enum Semantics {
        //all the The TemporalEvent qualifier takes a time period as a parameter

        FOLLOWED_BY, //The FOLLOWED BY operator specifies that first the left hand expression must turn true and only then is the right hand expression evaluated for matching events.

        WITHIN,//The WITHIN qualifier acts like a stopwatch. If the associated pattern expression does not become true within the specified time period it is evaluated by the engine as false.

        EVERY //The EVERY operator indicates that the pattern sub-expression should restart when the sub-expression qualified by the EVERY keyword evaluates to true or false. In the absence of the EVERY operator, an implicit EVERY operator is inserted as a qualifier to the first event stream source found in the pattern not occurring within a NOT expression.
    }

}
