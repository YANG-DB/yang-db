package com.kayhut.fuse.model.asgQuery;

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.model.query.quant.QuantType;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by benishue on 23-Feb-17.
 */
public class AsgQuery {

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AsgEBase<Start> getStart() {
        return start;
    }

    public void setStart(AsgEBase<Start> start) {
        this.start = start;
    }

    public static final class AsgQueryBuilder {
        //region Fields
        private String ont;
        private String name;
        private AsgEBase start;

        private AsgQueryBuilder() {
        }

        public static AsgQueryBuilder anAsgQuery() {
            return new AsgQueryBuilder();
        }

        public AsgQueryBuilder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public AsgQueryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AsgQueryBuilder withStart(AsgEBase<Start> start) {
            this.start = start;
            return this;
        }

        public AsgQuery build() {
            AsgQuery asgQuery = new AsgQuery();
            asgQuery.setOnt(ont);
            asgQuery.setName(name);
            asgQuery.setStart(start);
            return asgQuery;
        }
    }

    //region Fields
    private String ont;
    private String name;
    private AsgEBase<Start> start;
    //endregion


    public static class Builder {

        private AsgQuery query;
        private AsgEBase current;

        private Builder(String queryName, String ontologyName) {
            query = new AsgQuery();
            Start start = new Start();
            start.seteNum(0);
            AsgEBase<Start> eBase = new AsgEBase<>(start);

            current = eBase;
            query.setStart(eBase);
            query.setName(queryName);
            query.setOnt(ontologyName);
        }

        public static Builder start(String queryName, String ontologyName) {
            return new Builder(queryName,ontologyName);
        }

        private AsgEBase addNextChild(AsgEBase element,boolean within) {
            current.addNextChild(element);
            if(!within) {
                current = element;
            }
            return current;
        }

        private AsgEBase addNextB(AsgEBase<RelProp> element) {
            current.addBChild(element);
            current = element;
            return current;
        }

        public static AsgEBase<EConcrete> concrete(String eID, int eType, String eName, String eTag, int eNum) {
            EConcrete concrete = new EConcrete();
            concrete.seteType(eType);
            concrete.seteID(eID);
            concrete.seteName(eName);
            concrete.seteTag(eTag);
            concrete.seteNum(eNum);

            return new AsgEBase<>(concrete);
        }

        public Builder in(AsgEBase ... eBase) {
            Arrays.asList(eBase).forEach(element -> addNextChild(element,true));
            return this;
        }

        public Builder next(AsgEBase eBase) {
            addNextChild(eBase,false);
            return this;
        }

        public Builder below(AsgEBase eBase) {
            addNextB(eBase);
            return this;
        }


        public static AsgEBase<Quant1> quant1(int eNum, QuantType type) {
            Quant1 quant1  = new Quant1();
            quant1.seteNum(eNum);
            quant1.setqType(type);

            return new AsgEBase<>(quant1);
        }

        public static AsgEBase<Quant2> quant2(int eNum, QuantType type) {
            Quant2 quant2 = new Quant2();
            quant2.seteNum(eNum);
            quant2.setqType(type);

            return new AsgEBase<>(quant2);
        }

        public static AsgEBase<ETyped> typed(int eType,String eTag, int eNum) {
            ETyped eTyped = new ETyped();
            eTyped.seteType(eType);
            eTyped.seteTag(eTag);
            eTyped.seteNum(eNum);

            return new AsgEBase<>(eTyped);
        }

        public static AsgEBase<EUntyped> unTyped(String eTag, int eNum,Integer ... vTypes) {
            EUntyped untyped = new EUntyped();
            untyped.setNvTypes(Arrays.asList(vTypes));
            untyped.seteTag(eTag);
            untyped.seteNum(eNum);

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<Rel> rel(Rel.Direction direction, int eNum, int rType) {
            Rel rel = new Rel();
            rel.setDir(direction);
            rel.setrType(rType);
            rel.seteNum(eNum);

            return new AsgEBase<>(rel);
        }

        public static AsgEBase<EPropGroup> eProp(int eNum, EProp... props) {
            EPropGroup group = new EPropGroup();
            group.seteNum(eNum);
            group.seteProps(Arrays.asList(props));

            return new AsgEBase<>(group);
        }

        public static AsgEBase<RelPropGroup> relProp(int eNum, RelProp ... props) {
            RelPropGroup relPropGroup = new RelPropGroup();
            relPropGroup.seteNum(eNum);
            relPropGroup.setrProps(Arrays.asList(props));

            return new AsgEBase<>(relPropGroup);
        }

        public AsgQuery build() {
            return query;
        }

    }
}
