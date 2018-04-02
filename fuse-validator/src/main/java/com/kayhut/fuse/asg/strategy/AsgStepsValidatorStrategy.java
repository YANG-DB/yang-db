package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.Utils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.validation.ValidationResult.OK;

public class AsgStepsValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains two adjacent Entities without relation inside ";
    public static final String ERROR_2 = "Ontology Contains two adjacent Relations without Entity inside ";


    public static final String REL = Relation.class.getSimpleName();
    public static final String REL_PROP = RelPropGroup.class.getSimpleName();

    public static final String ENTITY = EEntityBase.class.getSimpleName();
    public static final String ENTITY_PROPS = EPropGroup.class.getSimpleName();


    public static final String FULL_STEP_DOUBLE_REL = REL+"((:"+REL_PROP+")?):"+REL+"((:"+REL_PROP+")?)";

    public static final String FULL_STEP_DOUBLE_ENTITY = ENTITY+"((:"+ENTITY_PROPS+")?):"+ENTITY+"((:"+ENTITY_PROPS+")?)";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        String pattern = AsgQueryUtil.pattern(query);
        Optional<String> match1 = Utils.match(pattern, FULL_STEP_DOUBLE_ENTITY);
        if(match1.isPresent())
            errors.add(ERROR_1 +":" + match1.get());

        Optional<String> match2 = Utils.match(pattern,FULL_STEP_DOUBLE_REL);
        if(match2.isPresent())
            errors.add(ERROR_2 +":" + match2.get());

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, errors.toArray(new String[errors.size()]));
    }
    //endregion
}
