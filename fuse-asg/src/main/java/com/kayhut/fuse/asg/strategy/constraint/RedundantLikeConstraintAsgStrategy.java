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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * search for "like" / "likeAny" constraint within a EpropGroup that has "*" expression in it (in the list of expressions within likeAny)
 * if one found => remove the entire EpropGroup
 */
public class RedundantLikeConstraintAsgStrategy extends ConstraintTransformationAsgStrategyBase {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            //currently supporting only ETyped or EConcrete
            Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EEntityBase.class);
            if (!eTypedAsgEBase.isPresent()) {
                return;
            }

            List<EProp> eProps = ePropGroupAsgEBase.geteBase().getProps().stream()
                    .filter(p ->
                            (p.getCon().getOp().equals(ConstraintOp.like) || p.getCon().getOp().equals(ConstraintOp.likeAny)))
                    .filter(p -> (
                            p.getCon().getOp().equals(ConstraintOp.like) &&
                                    p.getCon().getExpr().toString().matches("[*]+")) ||
                            p.getCon().getOp().equals(ConstraintOp.likeAny) &&
                                    ((List<String>) p.getCon().getExpr()).stream().anyMatch(s -> s.matches("[*]+")))
                    .collect(Collectors.toList());
            //remove all non needed '*' eProp
            ePropGroupAsgEBase.geteBase().getProps().removeAll(eProps);

        });
    }

    //region Private Methods


}




