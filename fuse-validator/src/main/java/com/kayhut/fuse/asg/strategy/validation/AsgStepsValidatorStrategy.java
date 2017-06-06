package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.asg.strategy.ValidationContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.Utils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.results.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.asg.strategy.ValidationContext.OK;

public class AsgStepsValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains two adjacent steps without relation inside ";

    static final String ENTITY_ONE = "entityOne";
    static final String OPTIONAL_ENTITY_ONE_FILTER = "optionalEntityOneFilter";
    static final String ENTITY_TWO = "entityTwo";
    static final String OPTIONAL_ENTITY_TWO_FILTER = "optionalEntityTwoFilter";


    public static final String FULL_STEP = "(?<" + ENTITY_ONE + ">" + EEntityBase.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_ENTITY_ONE_FILTER + ">" + EPropGroup.class.getSimpleName() + ":)?" +
            "(?<" + ENTITY_TWO + ">" + EEntityBase.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER + ">" + EPropGroup.class.getSimpleName() + "))?";

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        String pattern = AsgQueryUtil.pattern(query);
        Optional<String> match = Utils.match(pattern, FULL_STEP);
        if(match.isPresent())
            errors.add(ERROR_1 +":" + match.get());

        if (errors.isEmpty())
            return OK;

        return new ValidationContext(false, errors.toArray(new String[errors.size()]));
    }
    //endregion
}
