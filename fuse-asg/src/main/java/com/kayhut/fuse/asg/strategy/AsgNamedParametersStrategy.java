package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;

public class AsgNamedParametersStrategy implements AsgStrategy {
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        if(query.getParameters().isEmpty())
            return;

        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> {
                    String name = ((Map<String,Object>) eProp.getCon().getExpr()).values().iterator().next().toString();
                    Optional<NamedParameter> parameter = query.getParameters().stream().filter(p -> p.getName().equals(name)).findAny();
                    parameter.ifPresent(namedParameter -> eProp.setCon(Constraint.of(eProp.getCon().getOp(),namedParameter.getValue(),eProp.getCon().getiType())));
                });

    }
}
