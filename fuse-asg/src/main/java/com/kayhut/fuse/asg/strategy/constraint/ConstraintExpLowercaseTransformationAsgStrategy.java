package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

/**
 * transforms all e.value string expressions to lower case
 */
public class ConstraintExpLowercaseTransformationAsgStrategy extends ConstraintTransformationAsgStrategyBase {

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
        if (klass == EProp.class || klass == RelProp.class) {
            EProp eProp = (EProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());
            final Constraint con = eProp.getCon();
            if (property.isPresent() && property.get().getType().equals("string")) {
                if (con.getExpr() instanceof List) {
                    con.setExpr(Stream.ofAll((List) con.getExpr()).map(e->e.toString().toLowerCase()).toJavaList());
                } else if(con.getExpr() instanceof String){
                    con.setExpr(con.getExpr().toString().toLowerCase());
                }
            }
        }
    }

    //endregion

}




