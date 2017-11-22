package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kayhut.fuse.model.query.aggregation.*;
import com.kayhut.fuse.model.query.combiner.EComb;
import com.kayhut.fuse.model.query.combiner.RComb;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.HQuant;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;

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
        @JsonSubTypes.Type(name = "AggM5", value = AggM5.class),
        @JsonSubTypes.Type(name = "OptionalComp", value = OptionalComp.class)
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.geteNum() + ")";
    }
    //endregion

    //region Fields
    private int eNum;
    //endregion

}
