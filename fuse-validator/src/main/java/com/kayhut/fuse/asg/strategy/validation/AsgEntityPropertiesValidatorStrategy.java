package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.asg.strategy.ValidationContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kayhut.fuse.asg.strategy.ValidationContext.OK;
import static com.kayhut.fuse.asg.strategy.ValidationContext.print;

public class AsgEntityPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "No Parent Element found  ";
    public static final String ERROR_2 = "Property type mismatch parent entity";

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Ontology.Accessor accessor = context.getOntologyAccessor();
        Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .forEach(property -> {
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.nextAdjacentAncestor(property, EEntityBase.class);
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + ":" + property);
                    } else {
                        errors.addAll(check(accessor, parent.get(), property.geteBase()));
                    }
                });

        Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .forEach(group -> {
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.nextAdjacentAncestor(group, EEntityBase.class);
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + group);
                    } else {
                        errors.addAll(check(accessor, parent.get(), group.geteBase()));
                    }
                });
        if (errors.isEmpty())
            return OK;

        return new ValidationContext(false, errors.toArray(new String[errors.size()]));
    }
    //endregion

    private List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EPropGroup property) {
        return property.getProps().stream().map(prop->check(accessor,base,prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EProp property) {
        List<String> errors = new ArrayList<>();
        if (base.geteBase() instanceof Typed.eTyped) {
            EntityType entityType = accessor.$entity$(((Typed.eTyped) base.geteBase()).geteType());
            int pType = Integer.valueOf(property.getpType());

            if (entityType.getProperties().stream().noneMatch(p -> p == pType)) {
                errors.add(ERROR_2 + ":" + print(base, property));
            }

        } else if (base.geteBase() instanceof EUntyped) {
            Stream<Integer> types = Stream.ofAll(((EUntyped) base.geteBase()).getvTypes()).map(accessor::$entity$).flatMap(EntityType::getProperties);
            int pType = Integer.valueOf(property.getpType());

            if (types.toJavaStream().noneMatch(p -> p == pType)) {
                errors.add(ERROR_2 + ":" + print(base, property));
            }
        }
        return errors;
    }
}
