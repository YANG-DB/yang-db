package com.kayhut.fuse.asg.strategy.ConstraintTransformation;

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

import java.util.*;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgConstraintTypeTransformationStrategy extends AsgConstraintTransformationBase implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        getEprops(query).forEach(eProp -> {
            applyExpressionTransformation(context, eProp, EProp.class);
        });

        getRelProps(query).forEach(relProp -> {
            applyExpressionTransformation(context, relProp, RelProp.class);
        });
    }

    //region Private Methods

    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (klass == EProp.class){
            EProp eProp = (EProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(Integer.parseInt(eProp.getpType()));
            ConstraintOp op = eProp.getCon().getOp();
            if (property.isPresent() && isSingleElementOp(op)) {
                Constraint newCon = new Constraint(op, new OntologyPropertyTypeFactory().supply(property.get(), eProp.getCon().getExpr()));
                eProp.setCon(newCon);
            }
        }
        if (klass == RelProp.class) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(Integer.parseInt(relProp.getpType()));
            ConstraintOp op = relProp.getCon().getOp();
            if (property.isPresent() && isSingleElementOp(op)) {
                Constraint newCon = new Constraint(op, new OntologyPropertyTypeFactory().supply(property.get(), relProp.getCon().getExpr()));
                relProp.setCon(newCon);
            }
        }
    }

    private boolean isSingleElementOp(ConstraintOp op)
    {
        if (op == ConstraintOp.eq ||
            op == ConstraintOp.ne ||
            op == ConstraintOp.gt ||
            op == ConstraintOp.ge ||
            op == ConstraintOp.lt ||
            op == ConstraintOp.le ||
            op == ConstraintOp.contains ||
            op == ConstraintOp.startsWith ||
            op == ConstraintOp.notContains ||
            op == ConstraintOp.notStartsWith ||
            op == ConstraintOp.notEndsWith ||
            op == ConstraintOp.fuzzyEq ||
            op == ConstraintOp.fuzzyNe ||
            op == ConstraintOp.match ||
            op == ConstraintOp.notMatch ||
            op == ConstraintOp.empty ||
            op == ConstraintOp.notEmpty
            ) {
            return true;
        }
        return false;
    }
    //endregion

}




