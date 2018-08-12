package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.BaseProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;

import java.util.*;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getRelProps;

/**
 * transforms all e.value string expressions to lower case
 */
public class ConstraintExpLowercaseTransformationAsgStrategy implements AsgStrategy {
    private final Set<String> fields;

    public ConstraintExpLowercaseTransformationAsgStrategy(Collection<String> fields) {
        this.fields = new HashSet<>(fields);
    }

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

}




