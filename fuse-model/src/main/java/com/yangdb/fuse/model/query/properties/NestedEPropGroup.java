package com.yangdb.fuse.model.query.properties;

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
 * ScoreEPropGroup.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.query.quant.QuantType;

/**
 * Eprop group belonging to a nested entity under the current Entity
 *
 */
public class NestedEPropGroup extends BasePropGroup   {

    public NestedEPropGroup(String eType) {
        this.eType = eType;
    }

    public NestedEPropGroup(int eNum, String eType) {
        super(eNum);
        this.eType = eType;
    }

    public NestedEPropGroup(EPropGroup group, String eType) {
        this(group.geteNum(),group.quantType,group.props,group.groups,eType);
    }

    public NestedEPropGroup(String eType, EProp... props) {
        super(props);
        this.eType = eType;
    }

    public NestedEPropGroup(Iterable<EProp> props, String eType) {
        super(props);
        this.eType = eType;
    }

    public NestedEPropGroup(int eNum, String eType, EProp... props) {
        super(eNum, props);
        this.eType = eType;
    }

    public NestedEPropGroup(int eNum, Iterable<EProp> props, String eType) {
        super(eNum, props);
        this.eType = eType;
    }

    public NestedEPropGroup(int eNum, QuantType quantType, Iterable<EProp> props, String eType) {
        super(eNum, quantType, props);
        this.eType = eType;    }

    public NestedEPropGroup(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups, String eType) {
        super(eNum, quantType, props, groups);
        this.eType = eType;    }

    public String geteType() {
        return eType;
    }


    //region Fields
    private String eType;
    //endregion

}
