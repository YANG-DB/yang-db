package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.validation.QueryValidation;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.kayhut.fuse.model.validation.QueryValidation.OK;
import static com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements;

public class AsgCycleValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains Cycle ";

    @Override
    public QueryValidation apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        List<AsgEBase> elements = elements(query.getStart(), AsgEBase::getB, AsgEBase::getNext, (asgEBase -> true), (asgEBase -> true), Collections.EMPTY_LIST);
        if(new java.util.HashSet<>(elements).size() < elements.size())
            errors.add(ERROR_1);


        if (errors.isEmpty())
            return OK;

        return new QueryValidation(false, errors.toArray(new String[errors.size()]));
    }
    //endregion
}