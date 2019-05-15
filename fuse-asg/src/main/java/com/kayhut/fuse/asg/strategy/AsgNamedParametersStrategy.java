package com.kayhut.fuse.asg.strategy;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.constraint.*;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;

public class AsgNamedParametersStrategy implements AsgStrategy {
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        if (query.getParameters().isEmpty())
            return;

        //first handle more specific OptionalUnaryParameterizedConstraint
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> OptionalUnaryParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> {
                    String name = ((Map<String, Object>) eProp.getCon().getExpr()).values().iterator().next().toString();
                    Optional<NamedParameter> parameter = query.getParameters().stream().filter(p -> p.getName().equals(name)).findAny();
                    if (parameter.isPresent()) {
                        final Optional<ConstraintOp> optional = ((OptionalUnaryParameterizedConstraint) eProp.getCon()).getOperations().stream()
                                .filter(op -> op.toString().equals(parameter.get().getValue().toString()))
                                .findAny();
                        if (optional.isPresent()) {
                            eProp.setCon(Constraint.of(optional.get()));
                        } else {
                            //not present set default value
                            eProp.setCon(Constraint.of(eProp.getCon().getOp()));
                        }
                    } else {
                        //not present set default value
                        eProp.setCon(Constraint.of(eProp.getCon().getOp()));
                    }
                });

        //handle ParameterizedConstraint
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> {
                    Object expr = eProp.getCon().getExpr();
                    String[] name = {"name"};
                    if(expr instanceof QueryNamedParameter) {
                        name[0] = ((QueryNamedParameter) expr).getName();
                    } else if(expr instanceof Map) {
                        name[0] = ((Map<String, Object>) expr).values().iterator().next().toString();
                    }
                    Optional<NamedParameter> parameter = query.getParameters().stream().filter(p -> p.getName().equals(name[0])).findAny();
                    parameter.ifPresent(namedParameter -> eProp.setCon(Constraint.of(eProp.getCon().getOp(), namedParameter.getValue(), eProp.getCon().getiType())));
                });

    }
}
