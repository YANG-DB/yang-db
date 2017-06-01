package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.*;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import javaslang.collection.Stream;

import java.util.Optional;

public class AsgEntityPropertiesValidationValidatorStrategy implements AsgValidatorStrategy {

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor accessor = context.getOntologyAccessor();
        Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .forEach(entityBase -> {
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.nextAdjacentAncestor(entityBase, EEntityBase.class);
                    if(!parent.isPresent()) {
                        throw new IllegalArgumentException("No Parent Element found for "+entityBase);
                    }
                    AsgEBase<EEntityBase> base = parent.get();
                    if(base.geteBase() instanceof ETyped) {

                    } else if (base.geteBase() instanceof EConcrete){
                    } else if (base.geteBase() instanceof EUntyped){

                    }
                });
        return new ValidationContext();
    }
    //endregion
}
