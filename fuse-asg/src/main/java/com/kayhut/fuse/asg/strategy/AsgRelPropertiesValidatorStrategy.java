package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements;
import static com.kayhut.fuse.model.validation.ValidationResult.OK;
import static com.kayhut.fuse.model.validation.ValidationResult.print;

public class AsgRelPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "Property type mismatch parent Relation ";
    public static final String ERROR_3 = "No %s type found for constraint %s";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();
        List<AsgEBase<Rel>> list = elements(query.getStart(), (asgEBase -> Collections.emptyList()), AsgEBase::getNext,
                e -> e.geteBase() instanceof Rel,
                asgEBase -> true, Collections.emptyList());

        list.forEach(rel -> {
            if(!rel.getB().isEmpty()) {
                AsgEBase<? extends EBase> asgEBase = rel.getB().get(0);
                if(asgEBase.geteBase() instanceof RelProp) {
                    errors.addAll(check(accessor, rel, ((RelProp) asgEBase.geteBase())));
                } else if(asgEBase.geteBase() instanceof RelPropGroup) {
                    errors.addAll(check(accessor, rel, ((RelPropGroup) asgEBase.geteBase())));
                }
            }
        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, errors.toArray(new String[errors.size()]));
    }
    //endregion

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelPropGroup property) {
        return property.getProps().stream().map(prop -> check(accessor, base, prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelProp property) {
        List<String> errors = new ArrayList<>();
        RelationshipType relationshipType = accessor.$relation$(base.geteBase().getrType());
        String pType = property.getpType();

        if (relationshipType.getProperties().stream().noneMatch(p -> p.equals(pType))) {
            errors.add(ERROR_2 + ":" + print(base, property));
        }

        //interval type
        if(property.getCon().getiType()==null) {
            errors.add(String.format(ERROR_3 ," interval type ",property));
        }

        //expresion
        if (!Arrays.asList(ConstraintOp.empty, ConstraintOp.notEmpty).contains(property.getCon().getOp())) {
            if (property.getCon().getExpr() == null) {
                errors.add(String.format(ERROR_3, " expression ", property));
            }
        }

        //operation
        if(property.getCon().getOp()==null) {
            errors.add(String.format(ERROR_3 ," operation ",property));
        }

        return errors;
    }
}
