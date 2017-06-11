package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.kayhut.fuse.dispatcher.utils.ValidationContext.OK;
import static com.kayhut.fuse.dispatcher.utils.ValidationContext.print;
import static com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements;

public class AsgRelPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "Property type mismatch parent Relation ";

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
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

        return new ValidationContext(false, errors.toArray(new String[errors.size()]));
    }
    //endregion

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelPropGroup property) {
        return property.getProps().stream().map(prop -> check(accessor, base, prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelProp property) {
        List<String> errors = new ArrayList<>();
        RelationshipType relationshipType = accessor.$relation$(base.geteBase().getrType());
        int pType = Integer.valueOf(property.getpType());

        if (relationshipType.getProperties().stream().noneMatch(p -> p == pType)) {
            errors.add(ERROR_2 + ":" + print(base, property));
        }
        return errors;
    }
}
