package com.yangdb.fuse.asg.strategy.constraint;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.asg.util.OntologyPropertyTypeFactory;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.getRelProps;

/**
 * Created by benishue on 11-May-17.
 */
public class ConstraintIterableTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ConstraintIterableTransformationAsgStrategy() {
        this.propertyTypeFactory = new OntologyPropertyTypeFactory();
        this.multiValueOps = ConstraintOp.multiValueOps;
    }
    //endregion

    //region ConstraintTransformationAsgStrategyBase implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(eProp -> applyArrayTransformation(eProp, context));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(relProp -> applyArrayTransformation(relProp, context));
    }
    //endregion

    //region Private Methods
    private void applyArrayTransformation(EBase eBase, AsgStrategyContext context) {
        if (eBase instanceof EProp) {
            EProp eProp = (EProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());
            Object expr = eProp.getCon().getExpr();
            if (expr != null) {
                ConstraintOp op = eProp.getCon().getOp();
                if (isArrayOrIterable(expr) && isMultivaluedOp(op) && property.isPresent() ) {
                    List<Object> newList = transformToNewList(property.get(), expr);
                    Constraint newCon = eProp.getCon().clone();
                    newCon.setExpr(newList);
                    eProp.setCon(newCon);
                }
            }
        }
        if (eBase instanceof RelProp) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(relProp.getpType());
            if(relProp.getCon() != null) {
                Object expr = relProp.getCon().getExpr();
                if (expr != null) {
                    ConstraintOp op = relProp.getCon().getOp();
                    if (isArrayOrIterable(expr) && isMultivaluedOp(op) && property.isPresent()) {
                        List<Object> newList = transformToNewList(property.get(), expr);
                        Constraint newCon = relProp.getCon().clone();
                        newCon.setExpr(newList);
                        relProp.setCon(newCon);
                    }
                }
            }
        }
    }

    private List<Object> transformToNewList(Property property, Object expr) {
        return (isArray(expr) ?
                Stream.of(convertToObjectArray(expr)) :
                Stream.ofAll((Iterable)expr))
                .map(obj -> propertyTypeFactory.supply(property, obj))
                .toJavaList();
    }

    private boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    private boolean isIterable(Object obj) {
        return Iterable.class.isAssignableFrom(obj.getClass());
    }

    private boolean isArrayOrIterable(Object obj) {
        return isArray(obj) || isIterable(obj);
    }

    private Object[] convertToObjectArray(Object array) {
        Class ofArray = array.getClass().getComponentType();
        if (ofArray.isPrimitive()) {
            List ar = new ArrayList();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                ar.add(Array.get(array, i));
            }
            return ar.toArray();
        }
        else {
            return (Object[]) array;
        }
    }

    private boolean isMultivaluedOp(ConstraintOp op){
        return this.multiValueOps.contains(op);
    }
    //endregion

    //region Fields
    private OntologyPropertyTypeFactory propertyTypeFactory;
    private Set<ConstraintOp> multiValueOps;
    //endregion
}
