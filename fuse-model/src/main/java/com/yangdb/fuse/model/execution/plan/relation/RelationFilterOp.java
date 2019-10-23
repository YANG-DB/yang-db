package com.yangdb.fuse.model.execution.plan.relation;

/*-
 * #%L
 * fuse-model
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

/*-
 *
 * RelationFilterOp.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.execution.plan.AsgEBasePlanOp;
import com.yangdb.fuse.model.execution.plan.Filter;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelPropGroup;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class RelationFilterOp extends AsgEBasePlanOp<RelPropGroup> implements Filter {
    //region Constructors
    public RelationFilterOp() {
        super(new AsgEBase<>());
    }

    public RelationFilterOp(AsgEBase<RelPropGroup> relPropGroup) {
        super(relPropGroup);
    }
    //endregion

    //region Properties
    public AsgEBase<Rel> getRel() {
        return rel;
    }

    public void setRel(AsgEBase<Rel> rel) {
        this.rel = rel;
    }
    //endregion

    //region Fields
    private AsgEBase<Rel> rel;
    //endregion
}
