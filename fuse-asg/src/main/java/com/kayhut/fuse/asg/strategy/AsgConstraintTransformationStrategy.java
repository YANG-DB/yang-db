package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.asg.util.OntologyPropertyTypeFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgConstraintTransformationStrategy implements AsgStrategy {

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
        if (klass == EProp.class)
        {
            EProp eProp = (EProp) eBase;
            Optional<Property> property = OntologyUtil.getProperty(context.getOntology(), Integer.parseInt(eProp.getpType()));
            if (property.isPresent()) {
                Constraint newCon = new Constraint(eProp.getCon().getOp(), new OntologyPropertyTypeFactory().supply(property.get(), eProp.getCon().getExpr()));
                eProp.setCon(newCon);
            }
        }
        if (klass == RelProp.class)
        {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = OntologyUtil.getProperty(context.getOntology(), Integer.parseInt(relProp.getpType()));
            if (property.isPresent()) {
                Constraint newCon = new Constraint(relProp.getCon().getOp(), new OntologyPropertyTypeFactory().supply(property.get(), relProp.getCon().getExpr()));
                relProp.setCon(newCon);
            }
        }
    }

    private List<EProp> getEprops(AsgQuery query) {
        List<EProp> eProps = Stream.ofAll(AsgQueryUtils.getElements(query, EProp.class))
                .map(eBaseAsgEBase -> (EProp)eBaseAsgEBase.geteBase()).toJavaList();

        List<EPropGroup> ePropsGroup = Stream.ofAll(AsgQueryUtils.getElements(query, EPropGroup.class))
                .map(ePropGroup -> ((EPropGroup)ePropGroup.geteBase())).toJavaList();
        List<EProp> eProps2 = Stream.ofAll(ePropsGroup).flatMap(EPropGroup::geteProps).toJavaList();

        return java.util.stream.Stream.concat(eProps.stream(), eProps2.stream()).collect(Collectors.toList());
    }

    private List<RelProp> getRelProps(AsgQuery query) {
        List<RelProp> relProps = Stream.ofAll(AsgQueryUtils.getElements(query, RelProp.class))
                .map(eBaseAsgEBase -> (RelProp)eBaseAsgEBase.geteBase()).toJavaList();
        List<RelPropGroup> relPropsGroup = Stream.ofAll(AsgQueryUtils.getElements(query, RelPropGroup.class))
                .map(relPropGroup -> ((RelPropGroup)relPropGroup.geteBase())).toJavaList();
        List<RelProp> relProps2 = Stream.ofAll(relPropsGroup).flatMap(RelPropGroup::getrProps).toJavaList();

        return java.util.stream.Stream.concat(relProps.stream(), relProps2.stream()).collect(Collectors.toList());
    }
    //endregion

}




