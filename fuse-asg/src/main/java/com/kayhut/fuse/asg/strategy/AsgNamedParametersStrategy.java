package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;

import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;

public class AsgNamedParametersStrategy implements AsgStrategy {
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        if(query.getParameters().isEmpty())
            return;

        getEprops(query).stream()
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> {
                    String name = ((ParameterizedConstraint) eProp.getCon()).getExpr().getName();
                    Optional<NamedParameter> parameter = query.getParameters().stream().filter(p -> p.getName().equals(name)).findAny();
                    parameter.ifPresent(namedParameter -> eProp.getCon().setExpr(namedParameter.getValue()));
                });

    }
}
