package com.kayhut.fuse.asg.validation;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.Collections;
import java.util.List;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.elements;
import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.kayhut.fuse.model.validation.ValidationResult.OK;

public class AsgStartEntityValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Name doesn't match query Ontology reference";
    public static final String ERROR_2 = "No Elements After Start Node";
    public static final String ERROR_3 = "Start Node must be first element";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor accessor = context.getOntologyAccessor();

        if (query.getStart().getNext().isEmpty())
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_2);
        if (!query.getOnt().equals(accessor.name()))
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_1);

        List<AsgEBase<EBase>> list = nextDescendants(query.getStart().getNext().get(0), Start.class);

        if (!list.isEmpty())
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_3);


        return OK;
    }
    //endregion
}
