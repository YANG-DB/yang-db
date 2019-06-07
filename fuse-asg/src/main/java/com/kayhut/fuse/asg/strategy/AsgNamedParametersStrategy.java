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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.*;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;

public class AsgNamedParametersStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        this.query = query;
        this.context = context;
        if (query.getParameters().isEmpty())
            return;

        //first handle more specific OptionalUnaryParameterizedConstraint
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> OptionalUnaryParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> manageOptionalUnaryParameterizedConstraint(new ArrayList<>(query.getParameters()), eProp));

        //handle ParameterizedConstraint
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> manageParameterizedConstraint(new ArrayList<>(query.getParameters()), eProp));
    }

    protected void manageParameterizedConstraint(List<NamedParameter> params, EProp eProp) {
        Object expr = eProp.getCon().getExpr();
        String[] name = {"name"};
        if (expr instanceof QueryNamedParameter) {
            name[0] = ((QueryNamedParameter) expr).getName();
        } else if (expr instanceof Map) {
            name[0] = ((Map<String, Object>) expr).values().iterator().next().toString();
        }
        Optional<NamedParameter> parameter = params.stream().filter(p -> p.getName().equals(name[0])).findAny();
        //in case of singular operator and list of operands - use union of conditions for each query pattern
        if(isArrayOrIterable(parameter.get().getValue()) && isSingleElementOp(eProp.getCon().getOp())) {
            AtomicInteger counter = new AtomicInteger(AsgQueryUtil.maxEntityNum(query));
            //repeat for each condition - union on all params
            AsgEBase<Quant1> quant = new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.some));
            AsgEBase<EBase> asgEBase = AsgQueryUtil.nextDescendant(query.getStart(), ETyped.class).get();
            ((Collection) parameter.get().getValue()).forEach(value->{
                //clone pattern for each value
                AsgEBase<EBase> pattern = AsgQueryUtil.deepCloneWithEnums(counter, asgEBase, t -> true, t -> true,true);
                //replace named parameter with value...
                Optional<EProp> constraint = AsgQueryUtil.getParameterizedConstraint(pattern, parameter.get());
                manageParameterizedConstraint(Collections.singletonList(new NamedParameter(parameter.get().getName(), value)),constraint.get());
               //add to union
                quant.addNext(pattern);
            });
            query.getStart().setNext(Collections.singletonList(quant));
        } else {
            parameter.ifPresent(namedParameter -> eProp.setCon(Constraint.of(eProp.getCon().getOp(), namedParameter.getValue(), eProp.getCon().getiType())));
        }
    }

    protected void manageOptionalUnaryParameterizedConstraint(List<NamedParameter> params, EProp eProp) {
        String name = ((Map<String, Object>) eProp.getCon().getExpr()).values().iterator().next().toString();
        Optional<NamedParameter> parameter = params.stream().filter(p -> p.getName().equals(name)).findAny();
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
    }


    private boolean isArrayOrIterable(Object obj) {
        return isArray(obj) || isIterable(obj);
    }

    private boolean isMultivaluedOp(ConstraintOp op) {
        return multiValueOps.contains(op);
    }

    private boolean isSingleElementOp(ConstraintOp op) {
        return singleValueOps.contains(op);
    }

    private boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    private boolean isIterable(Object obj) {
        return Iterable.class.isAssignableFrom(obj.getClass());
    }

    private AsgQuery query;
    private AsgStrategyContext context;

}
