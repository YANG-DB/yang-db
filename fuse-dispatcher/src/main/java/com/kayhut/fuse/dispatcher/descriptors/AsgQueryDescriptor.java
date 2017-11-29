package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

import javax.management.relation.Relation;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class AsgQueryDescriptor implements Descriptor<AsgQuery> {
    //region Descriptor Implementation
    @Override
    public String describe(AsgQuery query) {
        return patternValue(query);
    }
    //endregion

    //region Private Methods
    private String patternValue(AsgQuery query) {
        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(
                query.getStart(),
                AsgEBase::getB,
                AsgEBase::getNext,
                asgEBase -> true,
                asgEBase -> true,
                Collections.emptyList());

        StringJoiner joiner = new StringJoiner(":","","");
        elements.forEach(e-> {
            if(e.geteBase() instanceof EEntityBase)
                joiner.add(EEntityBase.class.getSimpleName() +"["+e.geteNum()+"]");
            else if(e.geteBase() instanceof Rel)
                joiner.add(Relation.class.getSimpleName()+"["+e.geteNum()+"]");
            else if(e.geteBase() instanceof EPropGroup)
                joiner.add(EPropGroup.class.getSimpleName()+"["+e.geteNum()+"]");
            else if(e.geteBase() instanceof RelPropGroup)
                joiner.add(RelPropGroup.class.getSimpleName()+"["+e.geteNum()+"]");
            else
                joiner.add(e.geteBase().getClass().getSimpleName()+"["+e.geteNum()+"]");
        });
        return joiner.toString();
    }
    //endregion
}
