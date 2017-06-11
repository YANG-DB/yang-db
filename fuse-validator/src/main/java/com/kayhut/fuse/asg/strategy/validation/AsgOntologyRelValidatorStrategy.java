package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.Typed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.dispatcher.utils.ValidationContext.OK;
import static com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements;

public class AsgOntologyRelValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "Ontology doesn't Allow Relation with No entity Attached to ";
    public static final String ERROR_2 = "Ontology doesn't Allow such Relation with Entities construct ";

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();
        List<AsgEBase<Rel>> list = elements(query.getStart(), (asgEBase -> Collections.emptyList()), AsgEBase::getNext,
                e -> e.geteBase() instanceof Rel,
                asgEBase -> true, Collections.emptyList());

        list.forEach(rel -> {
            Optional<AsgEBase<EEntityBase>> sideA = AsgQueryUtil.ancestor(rel,v->v.geteBase() instanceof EEntityBase);
            Optional<AsgEBase<EEntityBase>> sideB = AsgQueryUtil.nextAdjacentDescendant(rel, EEntityBase.class);
            if (!sideA.isPresent() || !sideB.isPresent())
                errors.add(ERROR_1 + ":" + rel);

            List<EPair> relAllowedPairs = accessor.$relation$(rel.geteBase().getrType()).getePairs();

            EPair ePair = new EPair();

            if (sideA.isPresent()) {
                if (Typed.eTyped.class.isAssignableFrom(sideA.get().geteBase().getClass())) {
                    ePair.seteTypeA(((Typed.eTyped) sideA.get().geteBase()).geteType());
                }
            }

            if (sideB.isPresent()) {
                if (Typed.eTyped.class.isAssignableFrom(sideB.get().geteBase().getClass())) {
                    ePair.seteTypeB(((Typed.eTyped) sideB.get().geteBase()).geteType());
                }
            }

            if (ePair.geteTypeA() > 0)
                if (relAllowedPairs.stream().noneMatch(p -> p.geteTypeA() == ePair.geteTypeA()))
                    errors.add(ERROR_2 + ":" + ValidationContext.print(sideA.get(), rel, sideB.get()));

            if (ePair.geteTypeB() > 0)
                if (relAllowedPairs.stream().noneMatch(p -> p.geteTypeB() == ePair.geteTypeB()))
                    errors.add(ERROR_2 + ":" + ValidationContext.print(sideA.get(), rel, sideB.get()));



        });

        if (errors.isEmpty())
            return OK;

        return new ValidationContext(false, errors.toArray(new String[errors.size()]));
    }
    //endregion
}
