package com.yangdb.fuse.epb.plan.estimation.cache;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class EntityFilterOpDescriptor implements Descriptor<EntityFilterOp> {
    //region Descriptor Implementation
    @Override
    public String describe(EntityFilterOp item) {
        StringBuilder sb = new StringBuilder();
        appendEPropGroup(sb, item.getAsgEbase().geteBase());

        return sb.toString();
    }
    //endregion

    //region Private Methods
    private void appendEPropGroup(StringBuilder sb, EPropGroup ePropGroup) {
        List<EProp> eprops = Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .sortBy(BaseProp::getpType).toJavaList();

        for(EProp eProp : eprops) {
            appendEProp(sb, eProp);
        }

        for(EPropGroup childGroup : ePropGroup.getGroups()) {
            sb.append("[");
            appendEPropGroup(sb, childGroup);
            sb.append("]");
        }
    }

    private void appendEProp(StringBuilder sb, EProp eProp) {
        sb.append(eProp.getpType()).append(".")
                .append(eProp.getCon().getOp().toString()).append(".");

        String value = null;
        if (eProp.getCon().getExpr() == null) {
            sb.append("null");
        } else if (List.class.isAssignableFrom(eProp.getCon().getExpr().getClass())) {
            Stream.ofAll((List)eProp.getCon().getExpr()).take(10)
                    .forEach(val -> {
                        if (val == null) {
                            sb.append("null").append(",");
                        } else {
                            sb.append(val).append(",");
                        }
                    });
        } else {
            sb.append(eProp.getCon().getExpr());
        }
        sb.append(";");
    }
    //endregion
}
