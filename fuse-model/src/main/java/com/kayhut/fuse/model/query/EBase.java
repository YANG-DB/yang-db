package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kayhut.fuse.model.query.aggregation.*;

/**
 * Created by User on 16/02/2017.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Start", value = Start.class),
        @JsonSubTypes.Type(name = "EConcrete", value = EConcrete.class),
        @JsonSubTypes.Type(name = "EAgg", value = EAgg.class),
        @JsonSubTypes.Type(name = "EComb", value = EComb.class),
        @JsonSubTypes.Type(name = "ELog", value = ELog.class),
        @JsonSubTypes.Type(name = "EProp", value = EProp.class),
        @JsonSubTypes.Type(name = "ETyped", value = ETyped.class),
        @JsonSubTypes.Type(name = "EUntyped", value = EUntyped.class),
        @JsonSubTypes.Type(name = "HQuant", value = HQuant.class),
        @JsonSubTypes.Type(name = "Quant1", value = Quant1.class),
        @JsonSubTypes.Type(name = "Quant2", value = Quant2.class),
        @JsonSubTypes.Type(name = "RComb", value = RComb.class),
        @JsonSubTypes.Type(name = "Rel", value = Rel.class),
        @JsonSubTypes.Type(name = "RelProp", value = RelProp.class),
        @JsonSubTypes.Type(name = "AggL1", value = AggL1.class),
        @JsonSubTypes.Type(name = "AggL2", value = AggL2.class),
        @JsonSubTypes.Type(name = "AggL3", value = AggL3.class),
        @JsonSubTypes.Type(name = "AggL4", value = AggL4.class),
        @JsonSubTypes.Type(name = "AggM1", value = AggM1.class),
        @JsonSubTypes.Type(name = "AggM2", value = AggM2.class),
        @JsonSubTypes.Type(name = "AggM3", value = AggM3.class),
        @JsonSubTypes.Type(name = "AggM4", value = AggM4.class),
        @JsonSubTypes.Type(name = "AggM5", value = AggM5.class)
})
public class EBase {
    //region Properties
    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    //endregion

    //region Fields
    private int eNum;
    private String type;
    //endregion
}
