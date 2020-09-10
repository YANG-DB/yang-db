package com.yangdb.fuse.model.ontology.epl;

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
