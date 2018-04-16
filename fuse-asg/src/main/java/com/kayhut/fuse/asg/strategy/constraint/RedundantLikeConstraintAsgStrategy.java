package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * search for "like" / "likeAny" constraint within a EpropGroup that has "*" expression in it (in the list of expressions within likeAny)
 * if one found => remove the entire Eprop
 */
public class RedundantLikeConstraintAsgStrategy extends ConstraintTransformationAsgStrategyBase {
    //region ConstraintTransformationAsgStrategyBase Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            List<EProp> eProps = Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                    .filter(eProp -> (
                            eProp.getCon().getOp().equals(ConstraintOp.like) &&
                                    eProp.getCon().getExpr().toString().matches("[*]+")) ||
                            eProp.getCon().getOp().equals(ConstraintOp.likeAny) &&
                                    Stream.ofAll((List<String>) eProp.getCon().getExpr())
                                            .filter(value -> value.matches("[*]+")).toJavaOptional().isPresent())
                    .toJavaList();

            //remove all non needed '*' eProp
            ePropGroupAsgEBase.geteBase().getProps().removeAll(eProps);

        });
    }
    //endregion


}




