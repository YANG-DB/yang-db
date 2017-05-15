package com.kayhut.fuse.asg.strategy.ConstraintTransformation;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.asg.util.OntologyPropertyTypeFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 11-May-17.
 */
public class AsgConstraintArrayTransformationStrategy extends AsgConstraintTransformationBase implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).forEach(eProp -> {
            applyArrayTransformation(eProp, context);
        });

        getRelProps(query).forEach(relProp -> {
            applyArrayTransformation(relProp, context);
        });
    }

    //region Private Methods
    private void applyArrayTransformation(EBase eBase, AsgStrategyContext context) {
        if (eBase instanceof EProp) {
            EProp eProp = (EProp) eBase;
            Optional<Property> property = OntologyUtil.getProperty(context.getOntology(), Integer.parseInt(eProp.getpType()));
            Object expr = eProp.getCon().getExpr();
            ConstraintOp op = eProp.getCon().getOp();
            if (isArray(expr) && isMultivaluedOp(op) && property.isPresent()) {
                List<Object> newList = transformToNewList(property.get(), expr);
                Constraint newCon = new Constraint(eProp.getCon().getOp(), newList);
                eProp.setCon(newCon);
            }
        }
        if (eBase instanceof RelProp) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = OntologyUtil.getProperty(context.getOntology(), Integer.parseInt(relProp.getpType()));
            Object expr = relProp.getCon().getExpr();
            ConstraintOp op = relProp.getCon().getOp();
            if (isArray(expr) && isMultivaluedOp(op) && property.isPresent()) {
                List<Object> newList = transformToNewList(property.get(), expr);
                Constraint newCon = new Constraint(relProp.getCon().getOp(), newList);
                relProp.setCon(newCon);
            }
        }
    }

    private List<Object> transformToNewList(Property property, Object expr) {
        List<Object> objects = Arrays.asList(convertToObjectArray(expr));
        List<Object> newList = new ArrayList<>();
        objects.forEach(o -> newList.add(new OntologyPropertyTypeFactory().supply(property, o)));
        return newList;
    }

    private boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
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

}
