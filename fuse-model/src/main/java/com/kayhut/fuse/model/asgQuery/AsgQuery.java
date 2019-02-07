package com.kayhut.fuse.model.asgQuery;

/*-
 * #%L
 * AsgQuery.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.query.properties.projection.Projection;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by benishue on 23-Feb-17.
 */
public class AsgQuery implements IQuery<AsgEBase<? extends EBase>>{

    //region Getters & Setters

    public String getOnt() {
        return ont;
    }

    public Collection<NamedParameter> getParameters() {
        return parameters;
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

    public void setParameters(Collection<NamedParameter> parameters) {
        this.parameters = parameters;
    }

    public Query getOrigin() {
        return origin;
    }

    public void setOrigin(Query origin) {
        this.origin = origin;
    }

    @Override
    public Collection<AsgEBase<? extends EBase>> getElements() {
        return elements;
    }

    public void setElements(Collection<AsgEBase<? extends EBase>> elements) {
        this.elements = elements;
    }

//endregion

    @Override
    public String toString() {
        return "AsgQuery{" +
                "ont='" + ont + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AsgQuery other = (AsgQuery)o;

        if (!this.ont.equals(other.ont)) {
            return false;
        }

        if (!this.name.equals(other.name)) {
            return false;
        }

        if (!this.start.equals(other.start)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.ont.hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + start.hashCode();
        return result;
    }

    //region Fields
    private String ont;
    private String name;
    private AsgEBase<Start> start;
    private Query origin;

    private Collection<NamedParameter> parameters = new ArrayList<>();
    private Collection<AsgEBase<? extends EBase>> elements = new ArrayList<>();

    //endregion

    //region Builders
    public static final class AsgQueryBuilder {
        private AsgQuery asgQuery ;

        private AsgQueryBuilder() {
            asgQuery = new AsgQuery();
        }

        public static AsgQueryBuilder anAsgQuery() {
            return new AsgQueryBuilder();
        }

        public AsgQueryBuilder withOnt(String ont) {
            this.asgQuery.ont = ont;
            return this;
        }

        public AsgQueryBuilder withOrigin(Query origin) {
            this.asgQuery.origin = origin;
            return this;
        }

        public AsgQueryBuilder withName(String name) {
            this.asgQuery.name = name;
            return this;
        }
        public AsgQueryBuilder withParams(Collection<NamedParameter> params) {
            this.asgQuery.parameters = params;
            return this;
        }

        public AsgQueryBuilder withStart(AsgEBase<Start> start) {
            this.asgQuery.start = start;
            this.asgQuery.elements.add(start);
            return this;
        }

        public AsgQueryBuilder withElements(Collection<AsgEBase<? extends EBase>> values) {
            this.asgQuery.elements = values;
            return this;
        }

        public AsgQuery build() {
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
            query.elements.add(eBase);

        }

        public static Builder start(String queryName, String ontologyName) {
            return new Builder(queryName,ontologyName);
        }

        private AsgEBase addNextChild(AsgEBase element, boolean within) {
            current.addNextChild(element);
            query.elements.add(element);
            if(!within) {
                current = element;
            }
            return current;
        }

        private AsgEBase addNextB(AsgEBase<RelProp> element) {
            current.addBChild(element);
            query.elements.add(element);
            current = element;
            return current;
        }

        public static AsgEBase<EConcrete> concrete(int eNum, String eID, String eType, String eName, String eTag) {
            EConcrete concrete = new EConcrete();
            concrete.seteNum(eNum);
            concrete.seteType(eType);
            concrete.seteID(eID);
            concrete.seteName(eName);
            concrete.seteTag(eTag);

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

        public static AsgEBase<ETyped> typed(int eNum, String eType, String eTag) {
            ETyped eTyped = new ETyped();
            eTyped.seteNum(eNum);
            eTyped.seteType(eType);
            eTyped.seteTag(eTag);

            return new AsgEBase<>(eTyped);
        }

        public static AsgEBase<ETyped> typed(int eNum, String eType) {
            ETyped eTyped = new ETyped();
            eTyped.seteNum(eNum);
            eTyped.seteType(eType);
            return new AsgEBase<>(eTyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum) {
            EUntyped untyped = new EUntyped();
            untyped.seteNum(eNum);

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, String eTag) {
            EUntyped untyped = new EUntyped();
            untyped.seteNum(eNum);
            untyped.seteTag(eTag);
            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, String... vTypes) {
            EUntyped untyped = new EUntyped();
            untyped.seteNum(eNum);
            untyped.setvTypes(Stream.of(vTypes).toJavaList());

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, String eTag, Iterable<String> vTypes) {
            EUntyped untyped = new EUntyped();
            untyped.setvTypes(Stream.ofAll(vTypes).toJavaList());
            untyped.seteNum(eNum);
            untyped.seteTag(eTag);

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<EUntyped> unTyped(int eNum, String eTag, Iterable<String> vTypes, Iterable<String> nvTypes) {
            EUntyped untyped = new EUntyped();
            untyped.setvTypes(Stream.ofAll(vTypes).toJavaList());
            untyped.setNvTypes(Stream.ofAll(nvTypes).toJavaList());
            untyped.seteNum(eNum);
            untyped.seteTag(eTag);

            return new AsgEBase<>(untyped);
        }

        public static AsgEBase<Rel> rel(int eNum, String rType, Rel.Direction direction) {
            Rel rel = new Rel();
            rel.setDir(direction);
            rel.setrType(rType);
            rel.seteNum(eNum);

            return new AsgEBase<>(rel);
        }

        public static AsgEBase<Rel> rel(int eNum, String rType, Rel.Direction direction,String wrapper) {
            Rel rel = new Rel();
            rel.setDir(direction);
            rel.setrType(rType);
            rel.seteNum(eNum);
            rel.setWrapper(wrapper);
            return new AsgEBase<>(rel);
        }

        public static AsgEBase<EProp> eProp(int eNum, String pType, Constraint constraint) {
            return new AsgEBase<>(EProp.of(eNum, pType, constraint));
        }

        public static AsgEBase<EProp> eProp(int eNum, String pType, Projection projection) {
            return new AsgEBase<>(EProp.of(eNum, pType, projection));
        }

        public static AsgEBase<EPropGroup> ePropGroup(int eNum) {
            return new AsgEBase<>(new EPropGroup(eNum));
        }

        public static AsgEBase<EPropGroup> ePropGroup(int eNum, EProp... props) {
            return new AsgEBase<>(new EPropGroup(eNum, Arrays.asList(props)));
        }

        public static AsgEBase<EPropGroup> ePropGroup(int eNum, EPropGroup... groups) {
            return new AsgEBase<>(new EPropGroup(eNum, QuantType.all, Collections.emptyList(), Arrays.asList(groups)));
        }

        public static AsgEBase<EPropGroup> ePropGroup(int eNum, QuantType quantType, EProp... props) {
            return new AsgEBase<>(new EPropGroup(eNum, quantType, Arrays.asList(props)));
        }

        public static AsgEBase<EPropGroup> ePropGroup(int eNum, QuantType quantType, EPropGroup... groups) {
            return new AsgEBase<>(new EPropGroup(eNum, quantType, Collections.emptyList(), Arrays.asList(groups)));
        }

        public static AsgEBase<EPropGroup> ePropGroup(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups) {
            return new AsgEBase<>(new EPropGroup(eNum, quantType, props, groups));
        }

        public static AsgEBase<RelPropGroup> relProp(int eNum, RelProp ... props) {
            RelPropGroup relPropGroup = new RelPropGroup(Arrays.asList(props));
            relPropGroup.seteNum(eNum);
            return new AsgEBase<>(relPropGroup);
        }

        public static AsgEBase<RelPropGroup> relPropGroup(int eNum, RelProp ... props) {
            RelPropGroup relPropGroup = new RelPropGroup(Arrays.asList(props));
            relPropGroup.seteNum(eNum);
            return new AsgEBase<>(relPropGroup);
        }

        public static AsgEBase<RelPropGroup> relPropGroup(int eNum, QuantType quantType, RelProp ... props) {
            RelPropGroup relPropGroup = new RelPropGroup(Arrays.asList(props));
            relPropGroup.seteNum(eNum);
            relPropGroup.setQuantType(quantType);
            return new AsgEBase<>(relPropGroup);
        }

        public static AsgEBase<OptionalComp> optional(int eNum) {
            OptionalComp optionalComp = new OptionalComp(eNum, 0);
            return new AsgEBase<>(optionalComp);
        }

        public AsgEBase _() {
            return current;
        }

        public AsgQuery build() {
            return query;
        }

    }
    //endregion

}
