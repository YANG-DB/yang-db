package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.QuantBase;

import javax.management.relation.Relation;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class QueryDescriptor implements Descriptor<Query> {
    //region Descriptor Implementation
    @Override
    public String describe(Query query) {
        return patternValue(query);
    }
    //endregion

    //region Private Methods
    private String patternValue(Query query) {
        List<EBase> elements = query.getElements()!=null ? query.getElements() : Collections.EMPTY_LIST;
        StringJoiner joiner = new StringJoiner(":","","");
        elements.forEach(e-> {
            if(e instanceof QuantBase) {
                List<Integer> next = ((Next<List>) e).getNext();
                String join = next.stream().map(Object::toString).collect(Collectors.joining("|"));
                joiner.add(e.getClass().getSimpleName() +"["+e.geteNum()+"]").add("{"+join+"}");
            }
            else if(e instanceof EEntityBase)
                joiner.add(EEntityBase.class.getSimpleName() +"["+e.geteNum()+"]");
            else if(e instanceof Rel)
                joiner.add(Relation.class.getSimpleName()+"["+e.geteNum()+"]");
            else if(e instanceof EPropGroup)
                joiner.add(EPropGroup.class.getSimpleName()+"["+e.geteNum()+"]");
            else if(e instanceof RelPropGroup)
                joiner.add(RelPropGroup.class.getSimpleName()+"["+e.geteNum()+"]");
            else
                joiner.add(e.getClass().getSimpleName()+"["+e.geteNum()+"]");
        });
        return joiner.toString();
    }
    //endregion

    public static String toString(Query query) {
        return new QueryDescriptor().describe(query);
    }
}
