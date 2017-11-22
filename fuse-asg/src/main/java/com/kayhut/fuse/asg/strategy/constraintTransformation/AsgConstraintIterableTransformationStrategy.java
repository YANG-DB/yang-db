package com.kayhut.fuse.asg.strategy.constraintTransformation;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.asg.util.OntologyPropertyTypeFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 11-May-17.
 */
public class AsgConstraintIterableTransformationStrategy extends AsgConstraintTransformationBase implements AsgStrategy {
    //region Constructors
    public AsgConstraintIterableTransformationStrategy() {
        this.propertyTypeFactory = new OntologyPropertyTypeFactory();
    }
    //endregion

    //region AsgConstraintTransformationBase implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).forEach(eProp -> {
            applyArrayTransformation(eProp, context);
        });

        getRelProps(query).forEach(relProp -> {
            applyArrayTransformation(relProp, context);
        });
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
                if (isArrayOrIterable(expr) && isMultivaluedOp(op) && property.isPresent()) {
                    List<Object> newList = transformToNewList(property.get(), expr);
                    Constraint newCon = new Constraint(eProp.getCon().getOp(), newList);
                    eProp.setCon(newCon);
                }
            }
        }
        if (eBase instanceof RelProp) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(relProp.getpType());
            Object expr = relProp.getCon().getExpr();
            if (expr != null) {
                ConstraintOp op = relProp.getCon().getOp();
                if (isArrayOrIterable(expr) && isMultivaluedOp(op) && property.isPresent()) {
                    List<Object> newList = transformToNewList(property.get(), expr);
                    Constraint newCon = new Constraint(relProp.getCon().getOp(), newList);
                    relProp.setCon(newCon);
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
        if (op == ConstraintOp.inRange ||
                op == ConstraintOp.notInRange ||
                op == ConstraintOp.inSet ||
                op == ConstraintOp.notInSet ||
                op == ConstraintOp.empty ||
                op == ConstraintOp.notEmpty
                ) {
            return true;
        }
        return false;
    }
    //endregion

    //region Fields
    private OntologyPropertyTypeFactory propertyTypeFactory;
    //endregion
}
