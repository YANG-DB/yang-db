package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.sun.javafx.binding.StringFormatter;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.dispatcher.utils.ValidationContext.OK;
import static com.kayhut.fuse.dispatcher.utils.ValidationContext.print;

public class AsgEntityPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "No Parent Element found  ";
    public static final String ERROR_2 = "Property type mismatch parent entity";
    public static final String ERROR_3 = "No %s type found for constraint %s";

    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Ontology.Accessor accessor = context.getOntologyAccessor();
        Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .forEach(property -> {
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.ancestor(property,v->v.geteBase() instanceof EEntityBase);
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + ":" + property);
                    } else {
                        errors.addAll(check(accessor, parent.get(), property.geteBase()));
                    }
                });

        Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .forEach(group -> {
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.ancestor(group,v->v.geteBase() instanceof EEntityBase);
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
            String pType = property.getpType();

            if (entityType.getProperties().stream().noneMatch(p -> p.equals(pType))) {
                errors.add(ERROR_2 + ":" + print(base, property));
            }

        } else if (base.geteBase() instanceof EUntyped) {
            Stream<String> types = Stream.ofAll(((EUntyped) base.geteBase()).getvTypes()).map(accessor::$entity$).flatMap(EntityType::getProperties);
            String pType = property.getpType();

            if (types.toJavaStream().noneMatch(p -> p.equals(pType))) {
                errors.add(ERROR_2 + ":" + print(base, property));
            }
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
