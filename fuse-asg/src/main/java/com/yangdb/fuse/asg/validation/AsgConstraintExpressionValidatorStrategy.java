package com.yangdb.fuse.asg.validation;

/*-
 *
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.control.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.ignorableConstraints;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;

public class AsgConstraintExpressionValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Constraint expression is not valid %s for %s ";


    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        AsgQueryUtil.getEprops(query)
                .stream()
                .filter(EProp::isConstraint)
                .filter(p->!ignorableConstraints.contains(p.getCon().getClass()))
                .forEach(ep-> validateConstraint(ep,errors));


        AsgQueryUtil.getRelProps(query)
                .stream()
                .filter(RelProp::isConstraint)
                .filter(p->!ignorableConstraints.contains(p.getCon().getClass()))
                .forEach(ep-> validateConstraint(ep,errors));


        if (errors.isEmpty()) return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }

    private void validateConstraint(BaseProp prop, List<String> errors) {
        if (ConstraintOp.singleValueOps.contains(prop.getCon().getOp())) {
            //constraints with empty expression
            if (ConstraintOp.noValueOps.contains(prop.getCon().getOp())) {
                if(prop.getCon().getExpr()!=null) errors.add(String.format(ERROR_1,prop.getCon().getOp().name(),prop.getpType()));
            } else {
                //constraints with single value expression
                if (prop.getCon().getExpr() == null)
                    errors.add(String.format(ERROR_1, prop.getCon().getOp().name(), prop.getpType()));
                else if (prop.getCon().getExpr() instanceof List)
                    errors.add(String.format(ERROR_1, prop.getCon().getOp().name(), prop.getpType()));
            }
        }
        if (ConstraintOp.multiValueOps.contains(prop.getCon().getOp())) {

            //constraints with multi exactly 2 value expression
            if (ConstraintOp.exactlyTwoValueOps.contains(prop.getCon().getOp())) {
                if(prop.getCon().getExpr()==null) errors.add(String.format(ERROR_1,prop.getCon().getOp().name(),prop.getpType()));
                else if(!(prop.getCon().getExpr() instanceof List)) errors.add(String.format(ERROR_1,prop.getCon().getOp().name(),prop.getpType()));
                else if(((List)prop.getCon().getExpr()).size()!=2) errors.add(String.format(ERROR_1,prop.getCon().getOp().name(),prop.getpType()));
            } else {
                //constraints with multi value expression
                if (prop.getCon().getExpr() == null)
                    errors.add(String.format(ERROR_1, prop.getCon().getOp().name(), prop.getpType()));
                else if (!(prop.getCon().getExpr() instanceof List))
                    errors.add(String.format(ERROR_1, prop.getCon().getOp().name(), prop.getpType()));
            }
        }
    }
    //endregion
}
