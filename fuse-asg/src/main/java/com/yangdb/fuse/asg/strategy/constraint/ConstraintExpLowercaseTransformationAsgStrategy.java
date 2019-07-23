package com.yangdb.fuse.asg.strategy.constraint;

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

import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;

import java.util.*;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.getRelProps;

/**
 * transforms all e.value string expressions to lower case
 */
public class ConstraintExpLowercaseTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ConstraintExpLowercaseTransformationAsgStrategy(Collection<String> fields) {
        this.fields = new HashSet<>(fields);
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> fields.contains(prop.getpType()))
                .forEach(eProp -> applyExpressionTransformation(context, eProp, BaseProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> fields.contains(prop.getpType()))
                .forEach(relProp -> applyExpressionTransformation(context, relProp, BaseProp.class));
    }
    //endregion

    //region Private Methods
    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (BaseProp.class.isAssignableFrom(klass)) {
            BaseProp eProp = (BaseProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());
            final Constraint con = eProp.getCon();
            if (con != null && property.isPresent() && property.get().getType().equals("string")) {
                if (con.getExpr() instanceof List) {
                    con.setExpr(Stream.ofAll((List) con.getExpr()).map(e->e.toString().toLowerCase()).toJavaList());
                } else if(con.getExpr() instanceof String){
                    con.setExpr(con.getExpr().toString().toLowerCase());
                }
            }
        }
    }
    //endregion

    //region Fields
    private final Set<String> fields;
    //endregion
}




