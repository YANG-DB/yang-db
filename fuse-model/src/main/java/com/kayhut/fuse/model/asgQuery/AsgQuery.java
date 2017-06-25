package com.kayhut.fuse.model.asgQuery;

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
import javaslang.collection.Array;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by benishue on 23-Feb-17.
 */
public class AsgQuery implements IQuery{

    //region Getters & Setters

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

    //endregion

    @Override
    public String toString() {
        return "AsgQuery{" +
                "ont='" + ont + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    //region Fields
    private String ont;
    private String name;
    private AsgEBase<Start> start;
    //endregion

    //region Builders
    public static final class AsgQueryBuilder {
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

        public static AsgEBase<EConcrete> concrete(int eNum, String eID, int eType, String eName, String eTag) {
            return concrete(eNum, eID, eType, eName, eTag, new String[0]);
        }

        public static AsgEBase<EConcrete> concrete(int eNum, String eID, int eType, String eName, String eTag, String...reportProps) {
            EConcrete concrete = new EConcrete();
            concrete.seteNum(eNum);
            concrete.seteType(eType);
            concrete.seteID(eID);
            concrete.seteName(eName);
            concrete.seteTag(eTag);
            concrete.setReportProps(Stream.of(reportProps).toJavaList());

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

        public static AsgEBase<ETyped> typed(int eNum, int eType, String eTag) {
            return typed(eNum, eType, eTag, new String[0]);
        }

        public static AsgEBase<ETyped> typed(int eNum, int eType, String eTag, String...reportProps) {
            ETyped eTyped = new ETyped();
            eTyped.seteNum(eNum);
            eTyped.seteType(eType);
            eTyped.seteTag(eTag);
            eTyped.setReportProps(Stream.of(reportProps).toJavaList());

            return new AsgEBase<>(eTyped);
        }

        public static AsgEBase<ETyped> typed(int eNum, int eType) {
            ETyped eTyped = new ETyped();
            eTyped.seteNum(eNum);
            eTyped.seteType(eType);
            return new AsgEBase<>(eTyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, String eTag, Integer ... vTypes) {
            EUntyped untyped = new EUntyped();
            untyped.setvTypes(Stream.of(vTypes).toJavaList());
            untyped.seteTag(eTag);
            untyped.seteNum(eNum);

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, String eTag, Iterable<String> reportProps, Integer ... vTypes) {
            EUntyped untyped = new EUntyped();
            untyped.setvTypes(Stream.of(vTypes).toJavaList());
            untyped.seteTag(eTag);
            untyped.seteNum(eNum);
            untyped.setReportProps(Stream.ofAll(reportProps).toJavaList());

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, Integer ... vTypes) {
            EUntyped untyped = new EUntyped();
            untyped.setvTypes(Stream.of(vTypes).toJavaList());
            untyped.seteNum(eNum);

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<Rel> rel(int eNum, int rType, Rel.Direction direction) {
            Rel rel = new Rel();
            rel.setDir(direction);
            rel.setrType(rType);
            rel.seteNum(eNum);

            return new AsgEBase<>(rel);
        }

        public static AsgEBase<EPropGroup> eProp(int eNum, EProp... props) {
            EPropGroup group = new EPropGroup(Arrays.asList(props));
            group.seteNum(eNum);
            return new AsgEBase<>(group);
        }

        public static AsgEBase<RelPropGroup> relProp(int eNum, RelProp ... props) {
            RelPropGroup relPropGroup = new RelPropGroup(Arrays.asList(props));
            relPropGroup.seteNum(eNum);
            return new AsgEBase<>(relPropGroup);
        }

        public AsgQuery build() {
            return query;
        }

    }
    //endregion

}
