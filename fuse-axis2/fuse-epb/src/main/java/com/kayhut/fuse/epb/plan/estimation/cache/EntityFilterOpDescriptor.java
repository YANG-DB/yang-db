package com.kayhut.fuse.epb.plan.estimation.cache;

import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.query.properties.BaseProp;
import com.kayhut.fuse.model.query.properties.EProp;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class EntityFilterOpDescriptor implements Descriptor<EntityFilterOp> {
    @Override
    public String describe(EntityFilterOp item) {
        List<EProp> eprops = Stream.ofAll(item.getAsgEbase().geteBase().getProps())
                .filter(eProp -> eProp.getCon() != null)
                .sortBy(BaseProp::getpType).toJavaList();

        StringBuilder sb = new StringBuilder();
        for(EProp eprop : eprops) {
            sb.append(eprop.getpType()).append(".")
                    .append(eprop.getCon().getOp().toString()).append(".");

            String value = null;
            if (eprop.getCon().getExpr() == null) {
                sb.append("null");
            } else if (List.class.isAssignableFrom(eprop.getCon().getExpr().getClass())) {
                Stream.ofAll((List)eprop.getCon().getExpr()).take(10)
                        .forEach(val -> {
                            if (val == null) {
                                sb.append("null").append(",");
                            } else {
                                sb.append(val).append(",");
                            }
                        });
            } else {
                sb.append(eprop.getCon().getExpr());
            }
            sb.append(";");
        }

        return sb.toString();
    }
}
