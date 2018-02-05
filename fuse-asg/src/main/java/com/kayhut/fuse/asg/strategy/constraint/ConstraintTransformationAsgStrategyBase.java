package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by benishue on 09-May-17.
 */
public abstract class AsgConstraintTransformationBase implements AsgStrategy {

    //region Protected Methods
    protected List<EProp> getEprops(AsgQuery query) {
        List<EProp> eProps = Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .map(eBaseAsgEBase -> (EProp)eBaseAsgEBase.geteBase()).toJavaList();

        List<EPropGroup> ePropsGroup = Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .map(ePropGroup -> ((EPropGroup)ePropGroup.geteBase())).toJavaList();
        List<EProp> eProps2 = Stream.ofAll(ePropsGroup).flatMap(EPropGroup::getProps).toJavaList();

        return java.util.stream.Stream.concat(eProps.stream(), eProps2.stream()).collect(Collectors.toList());
    }

    protected List<RelProp> getRelProps(AsgQuery query) {
        List<RelProp> relProps = Stream.ofAll(AsgQueryUtil.elements(query, RelProp.class))
                .map(eBaseAsgEBase -> (RelProp)eBaseAsgEBase.geteBase()).toJavaList();
        List<RelPropGroup> relPropsGroup = Stream.ofAll(AsgQueryUtil.elements(query, RelPropGroup.class))
                .map(relPropGroup -> ((RelPropGroup)relPropGroup.geteBase())).toJavaList();
        List<RelProp> relProps2 = Stream.ofAll(relPropsGroup).flatMap(RelPropGroup::getProps).toJavaList();

        return java.util.stream.Stream.concat(relProps.stream(), relProps2.stream()).collect(Collectors.toList());
    }
    //endregion
}




