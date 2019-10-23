package com.yangdb.fuse.model.query;

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
 * EBase.java - fuse-model - yangdb - 2,016
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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yangdb.fuse.model.query.aggregation.*;
import com.yangdb.fuse.model.query.combiner.EComb;
import com.yangdb.fuse.model.query.combiner.RComb;
import com.yangdb.fuse.model.query.entity.*;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.quant.HQuant;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.Quant2;


/**
 * Created by lior.perry on 16/02/2017.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Start", value = Start.class),
        @JsonSubTypes.Type(name = "EConcrete", value = EConcrete.class),
        @JsonSubTypes.Type(name = "EAgg", value = EAgg.class),
        @JsonSubTypes.Type(name = "EComb", value = EComb.class),
        @JsonSubTypes.Type(name = "EProp", value = EProp.class),
        @JsonSubTypes.Type(name = "EPropGroup", value = EPropGroup.class),
        @JsonSubTypes.Type(name = "ETyped", value = ETyped.class),
        @JsonSubTypes.Type(name = "EUntyped", value = EUntyped.class),
        @JsonSubTypes.Type(name = "HQuant", value = HQuant.class),
        @JsonSubTypes.Type(name = "Quant1", value = Quant1.class),
        @JsonSubTypes.Type(name = "Quant2", value = Quant2.class),
        @JsonSubTypes.Type(name = "RComb", value = RComb.class),
        @JsonSubTypes.Type(name = "Rel", value = Rel.class),
        @JsonSubTypes.Type(name = "RelPattern", value = RelPattern.class),
        @JsonSubTypes.Type(name = "EndPattern", value = EndPattern.class),
        @JsonSubTypes.Type(name = "RelProp", value = RelProp.class),
        @JsonSubTypes.Type(name = "RelPropGroup", value = RelPropGroup.class),
        @JsonSubTypes.Type(name = "AggL1", value = AggL1.class),
        @JsonSubTypes.Type(name = "AggL2", value = AggL2.class),
        @JsonSubTypes.Type(name = "AggL3", value = AggL3.class),
        @JsonSubTypes.Type(name = "AggL4", value = AggL4.class),
        @JsonSubTypes.Type(name = "AggM1", value = AggM1.class),
        @JsonSubTypes.Type(name = "AggM2", value = AggM2.class),
        @JsonSubTypes.Type(name = "AggM3", value = AggM3.class),
        @JsonSubTypes.Type(name = "AggM4", value = AggM4.class),
        @JsonSubTypes.Type(name = "AggM5", value = AggM5.class),
        @JsonSubTypes.Type(name = "OptionalComp", value = OptionalComp.class),
        @JsonSubTypes.Type(name = "CountComp", value = CountComp.class),
        @JsonSubTypes.Type(name = "SchematicEProp", value = SchematicEProp.class),
        @JsonSubTypes.Type(name = "SchematicRankedEProp", value = SchematicRankedEProp.class),
        @JsonSubTypes.Type(name = "ScoreEProp", value = ScoreEProp.class),
        @JsonSubTypes.Type(name = "CalculatedEProp", value = CalculatedEProp.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EBase {
    //region Constructros
    public EBase() {}

    public EBase(int eNum) {
        this.eNum = eNum;
    }
    //endregion

    //region Properties
    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EBase eBase = (EBase) o;

        return eNum == eBase.eNum;
    }

    public EBase clone() {
        return new EBase(eNum);
    }

    public EBase clone(int eNum) {
        return new EBase(eNum);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.geteNum() + ")";
    }

    @Override
    public int hashCode() {
        return this.eNum;
    }
    //endregion

    //region Fields
    private int eNum;
    //endregion

}
