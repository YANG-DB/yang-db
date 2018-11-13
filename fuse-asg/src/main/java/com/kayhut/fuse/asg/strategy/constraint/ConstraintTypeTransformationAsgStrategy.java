package com.kayhut.fuse.asg.strategy.constraint;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.asg.util.OntologyPropertyTypeFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
import javaslang.collection.Stream;

import java.util.*;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getRelProps;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;

/**
 * Created by benishue on 09-May-17.
 */
public class ConstraintTypeTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ConstraintTypeTransformationAsgStrategy() {
        this.singleValueOps = Stream.of(eq, ne, gt, ge, lt, le, contains, startsWith, notContains, notStartsWith, notEndsWith,
                fuzzyEq, fuzzyNe, match, notMatch, empty, notEmpty).toJavaSet();
    }
    //endregion

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(eProp -> applyExpressionTransformation(context, eProp, EProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(relProp -> applyExpressionTransformation(context, relProp, RelProp.class));
    }

    //region Private Methods

    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (klass == EProp.class){
            EProp eProp = (EProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());

            ConstraintOp op = eProp.getCon().getOp();
            if (property.isPresent() && isSingleElementOp(op) && !ParameterizedConstraint.class.isAssignableFrom(eProp.getCon().getClass())) {
                Constraint newCon = new Constraint(op, new OntologyPropertyTypeFactory().supply(property.get(), eProp.getCon().getExpr()));
                eProp.setCon(newCon);
            }
        }
        if (klass == RelProp.class) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(relProp.getpType());
            if(relProp.getCon() != null) {
                ConstraintOp op = relProp.getCon().getOp();
                if (property.isPresent() && isSingleElementOp(op) && !ParameterizedConstraint.class.isAssignableFrom(relProp.getCon().getClass())) {
                    Constraint newCon = new Constraint(op, new OntologyPropertyTypeFactory().supply(property.get(), relProp.getCon().getExpr()));
                    relProp.setCon(newCon);
                }
            }
        }
    }

    private boolean isSingleElementOp(ConstraintOp op) {
        return singleValueOps.contains(op);
    }
    //endregion

    //region Fields
    private Set<ConstraintOp> singleValueOps;
    //endregion

}




