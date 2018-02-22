package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;

import javax.management.relation.Relation;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
        for (int i = 0; i < elements.size(); i++) {
            AsgEBase<EBase> e = elements.get(i);
            if(e.geteBase() instanceof QuantBase) {
                List<AsgEBase<? extends EBase>> next = e.getNext();
                String join = next.stream().map(n->Integer.toString(n.geteNum())).collect(Collectors.joining("|"));
                joiner.add(e.geteBase().getClass().getSimpleName() +"["+e.geteNum()+"]").add("{"+join+"}");
            }
            else if(e.geteBase() instanceof EEntityBase) {
                String prefix = "";
                if((i>0 && ((elements.get(i-1).geteBase() instanceof RelPropGroup) || (elements.get(i-1).geteBase() instanceof Rel)))) {prefix = "==>";};
                joiner.add(prefix+EEntityBase.class.getSimpleName() + "[" + e.geteNum() + "]");
            } else if(e.geteBase() instanceof Rel)
                joiner.add("==>"+Relation.class.getSimpleName()+"["+e.geteNum()+"]");
            else if(e.geteBase() instanceof EPropGroup)
                joiner.add(EPropGroup.class.getSimpleName()+"["+e.geteNum()+"]");
            else if(e.geteBase() instanceof RelPropGroup)
                joiner.add(RelPropGroup.class.getSimpleName()+"["+e.geteNum()+"]");
            else
                joiner.add(e.geteBase().getClass().getSimpleName()+"["+e.geteNum()+"]");
        }
        return joiner.toString();
    }

    public static String toString(AsgQuery query) {
        return new AsgQueryDescriptor().describe(query);
    }
    //endregion
}
