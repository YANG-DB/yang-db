package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.asg.strategy.ValidationContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.asg.strategy.ValidationContext.OK;
import static com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements;

public class AsgStartEntityValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Name doesn't match query Ontology reference";
    public static final String ERROR_2 = "No Elements After Start Node";
    public static final String ERROR_3 = "Start Node must be first element";

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor accessor = context.getOntologyAccessor();

        if (query.getStart().getNext().isEmpty())
            return new ValidationContext(false, ERROR_2);
        if (!query.getOnt().equals(accessor.name()))
            return new ValidationContext(false, ERROR_1);

        List<AsgEBase<EBase>> list = elements(query.getStart().getNext().get(0),
                (asgEBase -> Collections.emptyList()), AsgEBase::getNext,
                e -> e.geteBase() instanceof Start,
                asgEBase -> true, Collections.emptyList());

        if (!list.isEmpty())
            return new ValidationContext(false, ERROR_3);


        return OK;
    }
    //endregion
}
